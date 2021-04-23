package gov.cms.mat.fhir.services.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = {UCUMValidationService.class})
@ActiveProfiles("test")
class UCUMValidationServiceTest {
    @Autowired
    UCUMValidationService ucumValidationService;

    @Test
    void validateOK() {
        assertEquals(Boolean.TRUE, ucumValidationService.validate("m"));
    }

    @Test
    void validateBAD() {
        assertEquals(Boolean.FALSE, ucumValidationService.validate("BAD_BOY"));
    }
}