package gov.cms.mat.cql.parsers;

import gov.cms.mat.fhir.services.cql.parser.CqlToMatXml;
import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VersionTest {
    @Test
    public void testVersionRegex() {
        Pattern p = CqlToMatXml.LIBRARY_VERSION_PATTERN;
        assertTrue(p.matcher("100.100.001").matches());
        assertTrue(p.matcher("1.100.000").matches());
        assertTrue(p.matcher("4.0.001").matches());
        assertTrue(p.matcher("0.0.001").matches());
        assertTrue(p.matcher("999.999.000").matches());
        assertTrue(p.matcher("999.999.999").matches());

        assertFalse(p.matcher("0.0").matches());
        assertFalse(p.matcher("0").matches());
        assertFalse(p.matcher("0.0.0").matches());
        assertFalse(p.matcher("0.0.00").matches());

        assertFalse(p.matcher("9.9").matches());
        assertFalse(p.matcher("9").matches());
        assertFalse(p.matcher("9.9.9").matches());
        assertFalse(p.matcher("9.9.99").matches());
    }
}
