package gov.cms.mat.cql;

import gov.cms.mat.cql.elements.*;
import gov.cms.mat.fhir.services.ResourceFileUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CqlParserTest implements ResourceFileUtil {
    String cql;
    CqlParser cqlParser;

    @BeforeEach
    void setUP() {
        cql = getStringFromResource("/qdm_comments.cql");
        cqlParser = new CqlParser(cql);
    }

    @Test
    void testIncludeParser() {
        List<IncludeProperties> includeProperties = cqlParser.getIncludes();

        assertEquals(7, includeProperties.size());

        includeProperties.forEach(IncludeProperties::setToFhir);

        includeProperties.forEach(cqlParser::setToFhir);

        String fhirCql = cqlParser.getCql();
        assertTrue(fhirCql.contains("// I am a comment"));
        assertTrue(fhirCql.contains("// include UNDER version '5.6.789' called UNDER"));

        String multiLineComment = "/*\n" +
                "include BAD_GUY version '5.6.999' called AWFUL\n" +
                "*/";
        assertTrue(fhirCql.contains(multiLineComment));

        includeProperties.forEach(include -> assertTrue(include.createCql().contains("_FHIR4 version ")));
    }

    @Test
    void testUsingParser() {
        UsingProperties usingProperties = cqlParser.getUsing();

        assertEquals("using QDM version '5.4' // I like five four", usingProperties.createCql());
        usingProperties.setToFhir();

        cqlParser.setToFhir(usingProperties);

        assertEquals("using FHIR version '4.0.1' // I like five four", usingProperties.createCql());

        String fhirCql = cqlParser.getCql();
        assertTrue(fhirCql.contains("// using QDM version '5.2'"));

        String multiLineComment = "/*\n" +
                "using QDM version '5.2'\n" +
                "*/";

    }

    @Test
    void testLibraryParser() {
        LibraryProperties libraryProperties = cqlParser.getLibrary();
        assertEquals("library MATGlobalCommonFunctions version '1.0.000' // this best lib ever", libraryProperties.createCql());

        String fhirCql = cqlParser.getCql();
        assertTrue(fhirCql.contains("// library MATGlobalCommonFunctions version '0.0.999'"));
    }


    @Test
    void testDefineParser() {
        List<DefineProperties> defineProperties = cqlParser.getDefines();
        assertEquals(9, defineProperties.size());

        defineProperties.forEach(DefineProperties::setToFhir);

        defineProperties.forEach(cqlParser::setToFhir);

        String fhirCql = cqlParser.getCql();
        assertTrue(fhirCql.contains("// I am define line 1"));
        assertTrue(fhirCql.contains("// I am define line 2"));
        assertTrue(fhirCql.contains("// I am define line 3"));
    }

    @Test
    void testUnionParser() {
        List<UnionProperties> unionProperties = cqlParser.getUnions();
        assertEquals(1, unionProperties.size());
        assertEquals(7, unionProperties.get(0).getLines().size());

        String fhirCql = cqlParser.getCql();
        assertTrue(fhirCql.contains("// union1"));
        assertTrue(fhirCql.contains("commented out union"));
        assertTrue(fhirCql.contains("// union2"));

        String multiLineComment = "define \"No VTE Prophylaxis Device Order\":\n" +
                "  ([\"ServiceRequest\": \"Venous foot pumps (VFP)\"]\n" +
                "    union [\"ServiceRequest\": \"Intermittent pneumatic compression devices Test\"]\n" +
                "    union [\"ServiceRequest\": \"Graduated compression stockings Test\"]\n" +
                "  ) DeviceOrder\n" +
                "    where DeviceOrder.status = 'completed'\n" +
                "      and DeviceOrder.doNotPerform is true\n" +
                "*/";

        assertTrue(fhirCql.contains(multiLineComment));
    }

    @Test
    void testCodeSystemParser() {
        List<CodeSystemProperties> unionProperties = cqlParser.getCodeSystems();
        assertEquals(4, unionProperties.size());

        String fhirCql = cqlParser.getCql();
        assertTrue(fhirCql.contains("// I am a codesystem"));

    }

    @Test
    void testValueSetParser() {
        List<ValueSetProperties> valueSetProperties = cqlParser.getValueSets();
        assertEquals(7, valueSetProperties.size());


        valueSetProperties.forEach(ValueSetProperties::setToFhir);

        valueSetProperties.forEach(cqlParser::setToFhir);

        String fhirCql = cqlParser.getCql();
        assertTrue(fhirCql.contains("// I am a valuesystem"));

    }
}