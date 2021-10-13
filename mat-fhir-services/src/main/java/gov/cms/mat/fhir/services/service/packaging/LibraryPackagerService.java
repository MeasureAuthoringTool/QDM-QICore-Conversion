package gov.cms.mat.fhir.services.service.packaging;

import com.sun.istack.NotNull;
import gov.cms.mat.fhir.commons.objects.FhirResourceValidationResult;
import gov.cms.mat.fhir.rest.dto.FhirIncludeLibraryReferences;
import gov.cms.mat.fhir.rest.dto.FhirIncludeLibraryResult;
import gov.cms.mat.fhir.services.components.fhir.FhirIncludeLibraryProcessor;
import gov.cms.mat.fhir.services.cql.CQLAntlrUtils;
import gov.cms.mat.fhir.services.cql.LibraryCqlVisitorFactory;
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
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LibraryPackagerService implements FhirLibraryHelper {
    private final HapiFhirServer hapiFhirServer;
    private final FhirIncludeLibraryProcessor fhirIncludeLibraryProcessor;
    private final FhirValidatorService fhirValidatorService;
    private final CQLAntlrUtils cqlAntlrUtils;
    private final LibraryCqlVisitorFactory libVisitorFactory;

    public LibraryPackagerService(HapiFhirServer hapiFhirServer,
                                  FhirIncludeLibraryProcessor fhirIncludeLibraryProcessor,
                                  FhirValidatorService fhirValidatorService,
                                  CQLAntlrUtils cqlAntlrUtils,
                                  LibraryCqlVisitorFactory libVisitorFactory) {
        this.hapiFhirServer = hapiFhirServer;
        this.fhirIncludeLibraryProcessor = fhirIncludeLibraryProcessor;
        this.fhirValidatorService = fhirValidatorService;
        this.cqlAntlrUtils = cqlAntlrUtils;
        this.libVisitorFactory = libVisitorFactory;
    }

    public Library packageMinimum(String id) {
        Library library = fetchLibraryFromHapi(id);

        FhirResourceValidationResult result = fhirValidatorService.validate(library);

        if (CollectionUtils.isNotEmpty(result.getValidationErrorList())) {
            boolean hasErrors = result.getValidationErrorList().stream().
                    anyMatch(ve -> !StringUtils.equals("WARNING", ve.getSeverity()) &&
                            !StringUtils.equals("INFORMATION", ve.getSeverity()));
            if (hasErrors) {
                log.error("Validation errors encountered: {}", result.getValidationErrorList());
                throw new HapiResourceValidationException(id, "Library");
            }
        }
        return library;
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

    Bundle buildIncludeBundle(Library measureLib, String id) {
        Bundle result = new Bundle();
        // Using Bryns connecthathon examples use Transactions so they work with hapi-fhir.
        result.setType(Bundle.BundleType.TRANSACTION);

        result.addEntry().setResource(measureLib).getRequest()
                .setUrl("Library/" + getFhirId(measureLib))
                .setMethod(Bundle.HTTPVerb.PUT);

        addIncludedLibraries(result, findIncludedFhirLibraries(measureLib), id);

        var cql = cqlAntlrUtils.getCql(measureLib);
        var pair = libVisitorFactory.visitAndCollateHumanReadable(cql);

        return result;
    }

    private FhirIncludeLibraryResult findIncludedFhirLibraries(Library library) {
        if (CollectionUtils.isEmpty(library.getContent())) {
            throw new LibraryAttachmentNotFoundException(library);
        } else {
            return findIncludedLibraries(library, fhirIncludeLibraryProcessor);
        }
    }

    private void addIncludedLibraries(Bundle libraryBundle, FhirIncludeLibraryResult fhirIncludeLibraryResult, String id) {
        Set<FhirIncludeLibraryReferences> libraryReferences = fhirIncludeLibraryResult.getLibraryReferences();

        List<String> missingLibs = libraryReferences.stream()
                .filter(r -> r.getLibrary() == null)
                .map(r -> r.getName() + '-' + r.getVersion())
                .collect(Collectors.toList());

        if (missingLibs.isEmpty()) {
            for (FhirIncludeLibraryReferences reference : libraryReferences) {
                Library library = reference.getLibrary();
                library.setId(createLibraryName(library));
                libraryBundle.addEntry().setResource(library)
                        .getRequest()
                        .setUrl("Library/" + getFhirId(library))
                        .setMethod(Bundle.HTTPVerb.PUT);
            }
        } else {
            String missing = String.join(", ", missingLibs);
            throw new FhirIncludeLibrariesNotFoundException(id, missing);
        }
    }

    private String getFhirId(Resource r) {
        return getString(r);
    }

    @NotNull
    public static String getString(Resource r) {
        String id = r.getId();
        int endIndex = id.indexOf("/_history");
        if (endIndex >= 0) {
            id = id.substring(0, endIndex);
        }
        int startIndex = id.lastIndexOf('/') + 1;
        return id.substring(startIndex);
    }
}
