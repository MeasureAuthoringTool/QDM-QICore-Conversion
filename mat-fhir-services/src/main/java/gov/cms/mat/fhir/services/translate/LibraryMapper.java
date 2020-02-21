package gov.cms.mat.fhir.services.translate;

import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Library;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LibraryMapper extends MapperBase {
    public LibraryMapper(HapiFhirServer hapiFhirServer) {
        super(hapiFhirServer, Library.class);
    }
}
