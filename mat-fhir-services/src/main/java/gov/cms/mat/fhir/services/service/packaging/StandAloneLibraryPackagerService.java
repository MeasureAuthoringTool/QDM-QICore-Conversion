package gov.cms.mat.fhir.services.service.packaging;

import gov.cms.mat.fhir.commons.objects.FhirResourceValidationResult;
import gov.cms.mat.fhir.rest.dto.FhirIncludeLibraryReferences;
import gov.cms.mat.fhir.rest.dto.FhirIncludeLibraryResult;
import gov.cms.mat.fhir.services.components.fhir.FhirIncludeLibraryProcessor;
import gov.cms.mat.fhir.services.exceptions.HapiResourceNotFoundException;
import gov.cms.mat.fhir.services.exceptions.HapiResourceValidationException;
import gov.cms.mat.fhir.services.exceptions.cql.LibraryAttachmentNotFoundException;
import gov.cms.mat.fhir.services.hapi.HapiFhirLinkProcessor;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.rest.support.FhirValidatorProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.CollectionUtils;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Library;
import org.springframework.stereotype.Service;

import java.util.List;

import static gov.cms.mat.fhir.services.translate.LibraryTranslatorBase.CQL_CONTENT_TYPE;

@Service
@Slf4j
public class StandAloneLibraryPackagerService implements FhirValidatorProcessor {

    private final HapiFhirServer hapiFhirServer;
    private final FhirIncludeLibraryProcessor fhirIncludeLibraryProcessor;
    private final HapiFhirLinkProcessor hapiFhirLinkProcessor;

    public StandAloneLibraryPackagerService(HapiFhirServer hapiFhirServer,
                                            FhirIncludeLibraryProcessor fhirIncludeLibraryProcessor,
                                            HapiFhirLinkProcessor hapiFhirLinkProcessor) {
        this.hapiFhirServer = hapiFhirServer;
        this.fhirIncludeLibraryProcessor = fhirIncludeLibraryProcessor;
        this.hapiFhirLinkProcessor = hapiFhirLinkProcessor;
    }

    public Bundle packageMinimum(String id) {
        Bundle libraryBundle = processBundle(id);

        FhirResourceValidationResult result = validateResource(libraryBundle, hapiFhirServer.getCtx());

        if (CollectionUtils.isEmpty(result.getValidationErrorList())) {
            return libraryBundle;
        } else {
            throw new HapiResourceValidationException(id, "Library");
        }
    }

    public Bundle processBundle(String id) {
        Bundle bundle = hapiFhirServer.getLibraryBundle(id);

        if (bundle.hasEntry()) {
            return buildLibraryBundle(bundle);
        } else {
            throw new HapiResourceNotFoundException(id, "Library");
        }
    }

    public Bundle buildLibraryBundle(Bundle bundle) {
        Bundle libraryBundle = new Bundle();
        libraryBundle.setType(Bundle.BundleType.COLLECTION);
        Library library = (Library) bundle.getEntry().get(0).getResource();
        library.setId(createLibraryName(library));
        libraryBundle.addEntry().setResource(library);
        return libraryBundle;
    }

    private String createLibraryName(Library library) {
        return library.getName() + "-" + library.getVersion();
    }

    public Bundle packageFull(String id) {
        Bundle bundle = packageMinimum(id);

        FhirIncludeLibraryResult result = findIncludedFhirLibraries((Library) bundle.getEntry().get(0).getResource());
        processIncludedLibraries(bundle, result);

        return bundle;
    }


    private FhirIncludeLibraryResult findIncludedFhirLibraries(Library library) {
        if (CollectionUtils.isEmpty(library.getContent())) {
            throw new LibraryAttachmentNotFoundException(library);
        } else {
            Attachment cqlAttachment = getCqlAttachment(library);
            byte[] cqlBytes = Base64.decodeBase64(cqlAttachment.getData());
            return fhirIncludeLibraryProcessor.findIncludedFhirLibraries(new String(cqlBytes));
        }
    }

    private Attachment getCqlAttachment(Library library) {
        return library.getContent().stream()
                .filter(a -> a.getContentType().equals(CQL_CONTENT_TYPE))
                .findFirst()
                .orElseThrow(() -> new LibraryAttachmentNotFoundException(library, CQL_CONTENT_TYPE));
    }

    private void processIncludedLibraries(Bundle libraryBundle, FhirIncludeLibraryResult fhirIncludeLibraryResult) {
        List<FhirIncludeLibraryReferences> libraryReferences = fhirIncludeLibraryResult.getLibraryReferences();

        for (FhirIncludeLibraryReferences reference : libraryReferences) {
            Library library = fhirIncludeLibraryProcessor.fetchLibraryBundle(reference.getVersion(), reference.getName());
            library.setId(createLibraryName(library));
            libraryBundle.addEntry().setResource(library);
        }
    }
}
