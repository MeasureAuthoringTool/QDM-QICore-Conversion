package gov.cms.mat.cql_elm_translation.cql_translator;

import gov.cms.mat.cql_elm_translation.ResourceFileUtil;
import gov.cms.mat.cql_elm_translation.exceptions.QdmVersionNotfound;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MatCqlSourceParserTest implements ResourceFileUtil {
    MatCqlSourceParser matCqlSourceParser;

    @Test
    void parse_Success() {
        String cqlData = getData("/test.cql");

        matCqlSourceParser = new MatCqlSourceParser(cqlData);

        String results = matCqlSourceParser.parseQdmVersion();
        assertEquals("5.5", results);
    }

    @Test
    void parse_Error() {
        String cqlData = "See Spot Run\nSee spot Jump\nSee Spot Roll Over";

        matCqlSourceParser = new MatCqlSourceParser(cqlData);

        Assertions.assertThrows(QdmVersionNotfound.class, () -> matCqlSourceParser.parseQdmVersion());
    }
}