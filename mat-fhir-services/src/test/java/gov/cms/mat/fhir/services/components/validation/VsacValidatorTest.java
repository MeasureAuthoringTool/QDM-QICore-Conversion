package gov.cms.mat.fhir.services.components.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VsacValidatorTest {

    @Test
    void testVsacCodeSystemValidatorException() {
        VsacValidator.VsacCodeSystemValidatorException vsacCodeSystemValidatorException =
                new VsacValidator.VsacCodeSystemValidatorException("oops2");
        assertEquals("oops2", vsacCodeSystemValidatorException.getMessage());
    }
}