package gov.cms.mat.cql_elm_translation.cql_translator;

import gov.cms.mat.cql_elm_translation.ResourceFileUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MatLibrarySourceParserTest implements ResourceFileUtil {
    MatLibrarySourceParser matLibrarySourceParser;

    @Test
    void parse_Success() {
        String cqlData = getData("/test.cql");

        matLibrarySourceParser = new MatLibrarySourceParser(cqlData);

        MatLibrarySourceParser.ParseResults results = matLibrarySourceParser.parse();
        assertEquals("5.5", results.qdmVersion);

        assertEquals(1, results.getLibraries().size());

        MatLibrarySourceParser.LibraryItem libraryItem = results.getLibraries().get(0);
        assertEquals("OpioidDataWithNoInjectableFormulationsAndBulprenorphine", libraryItem.getName());
        assertEquals("0.3.000", libraryItem.getLibraryVersion());
    }
}