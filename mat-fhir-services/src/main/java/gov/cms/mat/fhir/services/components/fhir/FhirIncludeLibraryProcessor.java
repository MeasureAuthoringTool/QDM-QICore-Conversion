package gov.cms.mat.fhir.services.components.fhir;

import gov.cms.mat.cql.CqlTextParser;
import gov.cms.mat.cql.elements.IncludeProperties;
import gov.cms.mat.cql.elements.LibraryProperties;
import gov.cms.mat.cql.elements.UsingProperties;
import gov.cms.mat.fhir.rest.dto.FhirIncludeLibraryReferences;
import gov.cms.mat.fhir.rest.dto.FhirIncludeLibraryResult;
import gov.cms.mat.fhir.services.exceptions.CqlNotFhirException;
import gov.cms.mat.fhir.services.exceptions.FhirLibraryNotFoundException;
import gov.cms.mat.fhir.services.exceptions.FhirNotUniqueException;
import gov.cms.mat.fhir.services.exceptions.cql.LibraryAttachmentNotFoundException;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.translate.creators.FhirLibraryHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Library;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class FhirIncludeLibraryProcessor implements FhirLibraryHelper {

    private final HapiFhirServer hapiFhirServer;

    public FhirIncludeLibraryProcessor(HapiFhirServer hapiFhirServer) {
        this.hapiFhirServer = hapiFhirServer;
    }

    public FhirIncludeLibraryResult findIncludedFhirLibraries(String cqlContent) {
        if (StringUtils.isEmpty(cqlContent)) {
            throw new IllegalArgumentException("CQL is null or empty");
        } else {
            return processContent(cqlContent);
        }
    }

    private FhirIncludeLibraryResult processContent(String cqlContent) {
        CqlTextParser cqlTextParser = new CqlTextParser(cqlContent);

        checkIsFhir(cqlTextParser);

        return processParser(cqlTextParser);
    }

    private void checkIsFhir(CqlTextParser cqlTextParser) {
        UsingProperties usingProperties = cqlTextParser.getUsing();

        if (!usingProperties.isFhir()) {
            throw new CqlNotFhirException(usingProperties);
        }
    }

    private FhirIncludeLibraryResult processParser(CqlTextParser cqlTextParser) {
        var fhirIncludeLibraryResult = new FhirIncludeLibraryResult();

        fhirIncludeLibraryResult.setOutcome(true);

        LibraryProperties libraryProperties = cqlTextParser.getLibrary();

        fhirIncludeLibraryResult.setLibraryName(libraryProperties.getName());
        fhirIncludeLibraryResult.setLibraryVersion(libraryProperties.getVersion());

        getIncludedLibraryReferences(cqlTextParser, fhirIncludeLibraryResult);

        return fhirIncludeLibraryResult;
    }

    private void getIncludedLibraryReferences(CqlTextParser cqlTextParser, FhirIncludeLibraryResult fhirIncludeLibraryResult) {
        for (IncludeProperties include : cqlTextParser.getIncludes()) {
            var fhirIncludeLibraryReferences = new FhirIncludeLibraryReferences();

            fhirIncludeLibraryReferences.setName(include.getName());
            fhirIncludeLibraryReferences.setVersion(include.getVersion());

            try {
                Bundle bundle = fetchBundle(include);

                Library library = (Library) bundle.getEntry().get(0).getResource();

                fhirIncludeLibraryReferences.setSearchResult(true);
                fhirIncludeLibraryReferences.setReferenceEndpoint(bundle.getEntry().get(0).getFullUrl());
                fhirIncludeLibraryReferences.setLibrary(library);

                // to check if any included library has any included libraries.
                if (CollectionUtils.isEmpty(library.getContent())) {
                    throw new LibraryAttachmentNotFoundException(library);
                } else {
                    Attachment cqlAttachment = findCqlAttachment(library, "text/cql");
                    String data = new String(cqlAttachment.getData(), StandardCharsets.UTF_8);
                    var cqlTextParserIncludedLibrary = new CqlTextParser(data);
                    getIncludedLibraryReferences(cqlTextParserIncludedLibrary, fhirIncludeLibraryResult);
                }
            } catch (Exception e) {
                log.debug("Error when processing include: {}", include);
                fhirIncludeLibraryReferences.setSearchResult(false);
                fhirIncludeLibraryResult.setOutcome(false);
            }
            fhirIncludeLibraryResult.getLibraryReferences().add(fhirIncludeLibraryReferences);
        }
    }

    private Bundle fetchBundle(IncludeProperties include) {
        Bundle bundle = hapiFhirServer.fetchLibraryBundleByVersionAndName(include.getVersion(), include.getName());

        if (CollectionUtils.isEmpty(bundle.getEntry())) {
            throw new FhirLibraryNotFoundException(include);
        }

        if (bundle.getEntry().size() > 1) {
            throw new FhirNotUniqueException(include.toString(), bundle.getEntry().size());
        }
        return bundle;
    }
}
