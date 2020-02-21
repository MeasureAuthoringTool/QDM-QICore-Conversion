package gov.cms.mat.fhir.services.translate;

import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Measure;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MeasureMapper extends MapperBase {
    public MeasureMapper(HapiFhirServer hapiFhirServer) {
        super(hapiFhirServer, Measure.class);
    }
}
