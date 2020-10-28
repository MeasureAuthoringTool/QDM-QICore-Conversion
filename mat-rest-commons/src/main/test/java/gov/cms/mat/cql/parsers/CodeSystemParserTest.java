package gov.cms.mat.cql.parsers;

import gov.cms.mat.cql.elements.CodeSystemProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CodeSystemParserTest {

    CodeSystemParser codeSystemParser = new CodeSystemParser() {
        @Override
        public String[] getLines() {
            return new String[0];
        }
    };

    @Test
    void buildCodeSystemPropertiesEnsureVersion() {

        String line = "codesystem \"SNOMEDCT:2017-09\": 'http://snomed.info/sct/731000124108' version 'urn:hl7:version:2016-09'";
        CodeSystemProperties codeSystemProperties = codeSystemParser.buildCodeSystemProperties(line);
        assertEquals(line, codeSystemProperties.getLine());
        assertEquals("SNOMEDCT:2017-09", codeSystemProperties.getName());
        assertEquals("http://snomed.info/sct/731000124108", codeSystemProperties.getUrnOid());
        assertEquals("urn:hl7:version:2016-09", codeSystemProperties.getVersion());

        assertEquals(line, codeSystemProperties.createCql());
    }

    @Test
    void buildCodeSystemPropertiesEnsureNoVersion() {

        String line = "codesystem \"SNOMEDCT\": 'http://snomed.info/sct/731000124108'";
        CodeSystemProperties codeSystemProperties = codeSystemParser.buildCodeSystemProperties(line);
        assertEquals(line, codeSystemProperties.getLine());
        assertEquals("SNOMEDCT", codeSystemProperties.getName());
        assertEquals("http://snomed.info/sct/731000124108", codeSystemProperties.getUrnOid());
        assertNull(codeSystemProperties.getVersion());

        assertEquals(line, codeSystemProperties.createCql());
    }
}