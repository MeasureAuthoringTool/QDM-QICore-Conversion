package gov.cms.mat.fhir.services.components.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VsacValidatorTest {

    @Test
    void fetchFiveMinuteTicket() {
        VsacValidator.ExpiredTicketException expiredTicketException =
                new VsacValidator.ExpiredTicketException("oops1");
        assertEquals("oops1", expiredTicketException.getMessage());

        VsacValidator.VsacCodeSystemValidatorException vsacCodeSystemValidatorException =
                new VsacValidator.VsacCodeSystemValidatorException("oops2");
        assertEquals("oops2", vsacCodeSystemValidatorException.getMessage());
    }
}