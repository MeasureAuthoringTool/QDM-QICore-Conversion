package gov.cms.mat.fhir.services.rest.support;

import gov.cms.mat.fhir.services.exceptions.InvalidVersionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CqlVersionConverterTest {
    CqlVersionConverterTester converterTester = new CqlVersionConverterTester();

    @Test
    void convert_Success() {
        String versionAndBuild = "4.1.000";
        BigDecimal expected = new BigDecimal("4.001");

        BigDecimal result = converterTester.convertVersionToBigDecimal(versionAndBuild);
        assertEquals(expected, result);

    }

    @Test
    void convert_Missing() {
        InvalidVersionException thrown =
                Assertions.assertThrows(InvalidVersionException.class, () -> {
                    converterTester.convertVersionToBigDecimal("");
                });
        assertTrue(thrown.getMessage().contains("Cannot find version"));
    }

    @Test
    void convert_Invalid() {
        InvalidVersionException thrown =
                Assertions.assertThrows(InvalidVersionException.class, () -> {
                    converterTester.convertVersionToBigDecimal("i_am_a_teapot");
                });
        assertTrue(thrown.getMessage().contains("Version can contain only numbers and two decimal points:"));
    }

    @Test
    void convert_InvalidChars() {
        InvalidVersionException thrown =
                Assertions.assertThrows(InvalidVersionException.class, () -> {
                    converterTester.convertVersionToBigDecimal("4.1.000A");
                });
        assertTrue(thrown.getMessage().contains("Version can contain only numbers and two decimal points:"));
    }

    private static class CqlVersionConverterTester implements CqlVersionConverter {
    }
}

