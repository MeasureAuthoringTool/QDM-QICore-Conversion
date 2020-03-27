package gov.cms.mat.fhir.services.components.fhir;

import gov.cms.mat.cql.CqlParser;
import gov.cms.mat.cql.elements.IncludeProperties;
import gov.cms.mat.cql.elements.LibraryProperties;
import gov.cms.mat.fhir.rest.dto.FhirIncludeLibraryReferences;
import gov.cms.mat.fhir.rest.dto.FhirIncludeLibraryResult;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
@Slf4j
public class FhirIncludeLibraryProcessor {

    private final HapiFhirServer hapiFhirServer;

    public FhirIncludeLibraryProcessor(HapiFhirServer hapiFhirServer) {
        this.hapiFhirServer = hapiFhirServer;
    }


    public FhirIncludeLibraryResult findIncludedFhirLibraries(String cqlContent) { // for fhr cql only
        FhirIncludeLibraryResult res = new FhirIncludeLibraryResult();
        List<FhirIncludeLibraryReferences> includeRefs = new ArrayList<FhirIncludeLibraryReferences>();
        String libraryName = "";
        String libraryVersion = "";
        boolean result = true;
        try {
            CqlParser cqlParser = new CqlParser(cqlContent);
            LibraryProperties props = cqlParser.getLibrary();
            libraryName = props.getName();
            libraryVersion = props.getVersion();

            res.setLibraryName(libraryName);
            res.setLibraryVersion(libraryVersion);

            List<IncludeProperties> includes = cqlParser.getIncludes();
            Iterator iter = includes.iterator();


            while (iter.hasNext()) {
                IncludeProperties include = (IncludeProperties) iter.next();
                FhirIncludeLibraryReferences iRefs = new FhirIncludeLibraryReferences();
                String includeLibraryName = include.getName();
                String includeLibraryVersion = include.getVersion();
                iRefs.setName(includeLibraryName);
                iRefs.setVersion(includeLibraryVersion);
                boolean includeResults = true;
                String referenceEndpoint = "";

                try {
                    Bundle bundle = hapiFhirServer.fetchLibraryBundleByVersionAndName(includeLibraryVersion, includeLibraryName);
                    referenceEndpoint = bundle.getEntry().get(0).getFullUrl();
                    iRefs.setReferenceEndpoint(referenceEndpoint);
                } catch (Exception nx) {
                    nx.printStackTrace();
                    includeResults = false;
                    result = includeResults;
                }

                iRefs.setSearchResult(includeResults);
                includeRefs.add(iRefs);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            log.warn("Failed to process library " + libraryName + " " + libraryVersion);
        }

        res.setOutcome(result);
        res.setLibraryReferences(includeRefs);
        return res;
    }

}
