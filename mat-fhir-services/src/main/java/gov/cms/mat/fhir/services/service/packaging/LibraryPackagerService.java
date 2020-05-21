package gov.cms.mat.fhir.services.service.packaging;

import gov.cms.mat.fhir.commons.objects.FhirResourceValidationResult;
import gov.cms.mat.fhir.rest.dto.FhirIncludeLibraryReferences;
import gov.cms.mat.fhir.rest.dto.FhirIncludeLibraryResult;
import gov.cms.mat.fhir.services.components.fhir.FhirIncludeLibraryProcessor;
import gov.cms.mat.fhir.services.exceptions.FhirIncludeLibrariesNotFoundException;
import gov.cms.mat.fhir.services.exceptions.FhirNotUniqueException;
import gov.cms.mat.fhir.services.exceptions.HapiResourceNotFoundException;
import gov.cms.mat.fhir.services.exceptions.HapiResourceValidationException;
import gov.cms.mat.fhir.services.exceptions.cql.LibraryAttachmentNotFoundException;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.rest.support.FhirValidatorProcessor;
import gov.cms.mat.fhir.services.rest.support.FhirValidatorService;
import gov.cms.mat.fhir.services.service.packaging.dto.LibraryPackageFullHapi;
import gov.cms.mat.fhir.services.translate.creators.FhirLibraryHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Library;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LibraryPackagerService implements FhirValidatorProcessor, FhirLibraryHelper {
    private final HapiFhirServer hapiFhirServer;
    private final FhirIncludeLibraryProcessor fhirIncludeLibraryProcessor;
    private final FhirValidatorService fhirValidatorService;

    public LibraryPackagerService(HapiFhirServer hapiFhirServer,
                                  FhirIncludeLibraryProcessor fhirIncludeLibraryProcessor,
                                  FhirValidatorService fhirValidatorService) {
        this.hapiFhirServer = hapiFhirServer;
        this.fhirIncludeLibraryProcessor = fhirIncludeLibraryProcessor;
        this.fhirValidatorService = fhirValidatorService;
    }

    public Library packageMinimum(String id) {
        Library library = fetchLibraryFromHapi(id);

        FhirResourceValidationResult result = fhirValidatorService.validate(library);

        if (CollectionUtils.isEmpty(result.getValidationErrorList())) {
            return library;
        } else {
            throw new HapiResourceValidationException(id, "Library");
        }
    }

    public Library fetchLibraryFromHapi(String id) {
        Bundle bundle = hapiFhirServer.getLibraryBundle(id);

        if (bundle.hasEntry()) {
            if (bundle.getEntry().size() > 1) {
                throw new FhirNotUniqueException("Library id:" + id, bundle.getEntry().size());
            } else {
                return (Library) bundle.getEntry().get(0).getResource();
            }
        } else {
            throw new HapiResourceNotFoundException(id, "Library");
        }
    }

    private String createLibraryName(Library library) {
        return library.getName() + "-" + library.getVersion();
    }

    public LibraryPackageFullHapi packageFull(String id) {
        Library library = packageMinimum(id);

        Bundle includedLibraryBundle = buildIncludeBundle(library, id);

        return LibraryPackageFullHapi.builder()
                .library(library)
                .includeBundle(includedLibraryBundle)
                .build();
    }

    Bundle buildIncludeBundle(Library library, String id) {
        Bundle includedLibraryBundle = new Bundle();
        includedLibraryBundle.setType(Bundle.BundleType.COLLECTION);

        FhirIncludeLibraryResult result = findIncludedFhirLibraries(library);
        processIncludedLibraries(includedLibraryBundle, result, id);
        return includedLibraryBundle;
    }

    private FhirIncludeLibraryResult findIncludedFhirLibraries(Library library) {
        if (CollectionUtils.isEmpty(library.getContent())) {
            throw new LibraryAttachmentNotFoundException(library);
        } else {
            return findIncludedLibraries(library, fhirIncludeLibraryProcessor);
        }
    }


    private void processIncludedLibraries(Bundle libraryBundle, FhirIncludeLibraryResult fhirIncludeLibraryResult, String id) {
        List<FhirIncludeLibraryReferences> libraryReferences = fhirIncludeLibraryResult.getLibraryReferences();

        List<String> missingLibs = libraryReferences.stream()
                .filter(r -> r.getLibrary() == null)
                .map(r -> r.getName() + '-' + r.getVersion())
                .collect(Collectors.toList());

        if (missingLibs.isEmpty()) {
            for (FhirIncludeLibraryReferences reference : libraryReferences) {
                Library library = reference.getLibrary();
                library.setId(createLibraryName(library));
                libraryBundle.addEntry().setResource(library);
            }
        } else {
            String missing = String.join(", ", missingLibs);
            throw new FhirIncludeLibrariesNotFoundException(id, missing);
        }
    }
}
