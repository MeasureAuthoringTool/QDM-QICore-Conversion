package gov.cms.mat.fhir.services.components.fhir;

import gov.cms.mat.cql.CqlParser;
import gov.cms.mat.cql.elements.IncludeProperties;
import gov.cms.mat.cql.elements.LibraryProperties;
import gov.cms.mat.cql.elements.UsingProperties;
import gov.cms.mat.fhir.rest.dto.FhirIncludeLibraryReferences;
import gov.cms.mat.fhir.rest.dto.FhirIncludeLibraryResult;
import gov.cms.mat.fhir.services.exceptions.CqlNotFhirException;
import gov.cms.mat.fhir.services.exceptions.FhirLibraryNotFoundException;
import gov.cms.mat.fhir.services.exceptions.FhirNotUniqueException;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FhirIncludeLibraryProcessor {

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
        CqlParser cqlParser = new CqlParser(cqlContent);

        checkIsFhir(cqlParser);

        return processParser(cqlParser);
    }

    private void checkIsFhir(CqlParser cqlParser) {
        UsingProperties usingProperties = cqlParser.getUsing();

        if (!usingProperties.isFhir()) {
            throw new CqlNotFhirException(usingProperties);
        }
    }


    private FhirIncludeLibraryResult processParser(CqlParser cqlParser) {
        FhirIncludeLibraryResult res = new FhirIncludeLibraryResult();

        boolean result = true;

        LibraryProperties libraryProperties = cqlParser.getLibrary();

        res.setLibraryName(libraryProperties.getName());
        res.setLibraryVersion(libraryProperties.getVersion());

        for (IncludeProperties include : cqlParser.getIncludes()) {
            FhirIncludeLibraryReferences libraryReferences = new FhirIncludeLibraryReferences();

            libraryReferences.setName(include.getName());
            libraryReferences.setVersion(include.getVersion());

            try {
                Bundle bundle = fetchBundle(include);
                libraryReferences.setSearchResult(true);
                libraryReferences.setReferenceEndpoint(bundle.getEntry().get(0).getFullUrl());
            } catch (Exception e) {
                log.debug("Error when processing include: {}", include);
                libraryReferences.setSearchResult(false);
                result = false;
            }

            res.getLibraryReferences().add(libraryReferences);
        }

        res.setOutcome(result);

        return res;
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
