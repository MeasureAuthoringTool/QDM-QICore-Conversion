package gov.cms.mat.fhir.services.cql;

import gov.cms.mat.fhir.services.exceptions.CqlParseException;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.io.Charsets;
import org.cqframework.cql.gen.cqlLexer;
import org.cqframework.cql.gen.cqlParser;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Library;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

@Service
@Slf4j
public class CQLAntlrUtils {
    public static final String CQL_CONTENT_TYPE = "text/cql";

    public String getCql(Library library) {
        Optional<Attachment> a = library.getContent().stream().filter(
                c -> c.getContentType().equals(CQL_CONTENT_TYPE)).findFirst();
        if (a.isPresent()) {
            return new String(a.get().getData(), StandardCharsets.UTF_8);
        } else {
            throw new CqlParseException("Could not find content " + CQL_CONTENT_TYPE + " in lib " +
                    library.getName() + "v" + library.getVersion());
        }
    }

    public cqlParser.LibraryContext getLibraryContextBytes(byte[] charBytes) {
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
