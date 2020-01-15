package gov.cms.mat.fhir.services.translate;

import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.translate.creators.FhirRemover;
import org.hl7.fhir.instance.model.api.IBaseResource;

public class MapperBase implements FhirRemover {
    final Class<? extends IBaseResource> resourceClass;
    private final HapiFhirServer hapiFhirServer;

    public MapperBase(HapiFhirServer hapiFhirServer, Class<? extends IBaseResource> resourceClass) {
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
