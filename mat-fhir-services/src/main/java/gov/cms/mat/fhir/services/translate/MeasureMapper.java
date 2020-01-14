package gov.cms.mat.fhir.services.translate;

import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.translate.creators.FhirRemover;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Measure;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MeasureMapper implements FhirRemover {

    private final HapiFhirServer hapiFhirServer;

    public MeasureMapper(HapiFhirServer hapiFhirServer) {
        this.hapiFhirServer = hapiFhirServer;
    }

    public int count() {
        return hapiFhirServer.count(Measure.class);
    }

    public int deleteAll() {
        return deleteAllResource(hapiFhirServer, Measure.class);
    }
}
