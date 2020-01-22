package gov.cms.mat.fhir.services.translate;

import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.translate.creators.FhirRemover;
import org.hl7.fhir.r4.model.Resource;

public class MapperBase implements FhirRemover {
    final Class<? extends Resource> resourceClass;
    private final HapiFhirServer hapiFhirServer;

    public MapperBase(HapiFhirServer hapiFhirServer, Class<? extends Resource> resourceClass) {
        this.hapiFhirServer = hapiFhirServer;
        this.resourceClass = resourceClass;
    }

    public int count() {
        return hapiFhirServer.count(resourceClass);
    }

    public int deleteAll() {
        return deleteAllResource(hapiFhirServer, resourceClass);
    }
}
