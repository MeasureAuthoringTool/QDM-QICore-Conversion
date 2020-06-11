package gov.cms.mat.fhir.services.cql;

import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.io.Charsets;
import org.cqframework.cql.gen.cqlLexer;
import org.cqframework.cql.gen.cqlParser;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;

@Service
@Slf4j
public class CQLAntlrUtils {

    public cqlParser.LibraryContext getLibraryContext(byte[] charBytes) {
        return getLibraryContext(new String(charBytes, Charsets.UTF_8));
    }

    public cqlParser.LibraryContext getLibraryContext(String cql) {
        try {
            cqlLexer lexer = new cqlLexer(new ANTLRInputStream(new StringReader(cql)));
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            cqlParser parser = new cqlParser(tokens);
            parser.setBuildParseTree(true);
            return parser.library();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
}
