package gov.cms.mat.cql;

import gov.cms.mat.fhir.services.ResourceFileUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class CqlParserTest implements ResourceFileUtil {

    @Test
    void getCql() {
        String cql = getStringFromResource("/test_std_includes.cql");

        CqlParser cqlParser = new CqlParser(cql);
        cqlParser.getDefines();
        assertNotNull(cql);
        cqlParser.getCql();
    }
}