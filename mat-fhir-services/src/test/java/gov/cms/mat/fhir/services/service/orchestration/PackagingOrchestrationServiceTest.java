package gov.cms.mat.fhir.services.service.orchestration;

import gov.cms.mat.fhir.rest.packaging.FhirPackage;
import gov.cms.mat.fhir.services.HapiFhirServerTest;
import gov.cms.mat.fhir.services.components.fhir.FhirIncludeLibraryProcessor;
import gov.cms.mat.fhir.services.hapi.HapiFhirLinkProcessor;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled
class PackagingOrchestrationServiceTest implements HapiFhirServerTest {

    private PackagingOrchestrationService packagingOrchestrationService;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();

        HapiFhirServer hapiFhirServer = createTestHapiServer();

        HapiFhirLinkProcessor hapiFhirLinkProcessor = new HapiFhirLinkProcessor(restTemplate, hapiFhirServer);
        FhirIncludeLibraryProcessor fhirIncludeLibraryProcessor = new FhirIncludeLibraryProcessor(hapiFhirServer);

        packagingOrchestrationService =
                new PackagingOrchestrationService(hapiFhirServer, hapiFhirLinkProcessor, fhirIncludeLibraryProcessor);
    }


    @Test
    void packageMinimum() {

        FhirPackage fhirPackage = packagingOrchestrationService.packageMinimum("89017cfc79f644b89174359f82b12b1e", "json");

        System.out.println(fhirPackage);

        assertEquals("8.001", fhirPackage.getPackagingOutcome().getVersion());
        assertEquals("packageMinimum", fhirPackage.getPackagingOutcome().getPackagingType());
        assertEquals("DischargedonAntithromboticTherapy", fhirPackage.getPackagingOutcome().getName());

    }
}