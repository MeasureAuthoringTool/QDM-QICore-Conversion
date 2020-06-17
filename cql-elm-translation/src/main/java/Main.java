import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.cqframework.cql.gen.cqlLexer;
import org.cqframework.cql.gen.cqlParser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;


public class Main {
    public static void main(String[] args) throws IOException {
        String inputFile = null;
        if (args.length > 0) {
            inputFile = args[0];
        }
        InputStream is = System.in;
        if (inputFile != null) {
            is = new FileInputStream(inputFile);
        }
        ANTLRInputStream input = new ANTLRInputStream(is);
        cqlLexer lexer = new cqlLexer(input);


        CommonTokenStream tokens = new CommonTokenStream(lexer);
        cqlParser parser = new cqlParser(tokens);


        parser.setBuildParseTree(true);
        cqlParser.LibraryContext tree = parser.library();

        List<cqlParser.IncludeDefinitionContext> d = tree.includeDefinition();
        List<cqlParser.ValuesetDefinitionContext> valuesetDefinitionContexts = tree.valuesetDefinition();

        valuesetDefinitionContexts.forEach(v -> System.out.println(v.identifier()));


        // show tree in text form
        System.out.println(tree.toStringTree(parser));
    }
}