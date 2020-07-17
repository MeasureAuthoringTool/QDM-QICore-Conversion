package gov.cms.mat.fhir.services.cql.parser;

import gov.cms.mat.fhir.services.components.conversion.ConversionDataComponent;
import gov.cms.mat.fhir.services.exceptions.CqlParseException;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.cqframework.cql.gen.cqlBaseListener;
import org.cqframework.cql.gen.cqlLexer;
import org.cqframework.cql.gen.cqlParser;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.cqframework.cql.gen.cqlLexer.IDENTIFIER;
import static org.cqframework.cql.gen.cqlLexer.QUOTEDIDENTIFIER;
import static org.cqframework.cql.gen.cqlLexer.WS;

@Slf4j
@Component
public class ConversionParserListener extends cqlBaseListener {

    private static final int COLON = 11;
    private static final int DOT = 17;
    private static final int OPEN_BRACKET = 37;
    private static final int CLOSED_BRACKET = 40;
    private final ConversionDataComponent conversionDataComponent;

    public ConversionParserListener(ConversionDataComponent conversionDataComponent) {
        this.conversionDataComponent = conversionDataComponent;
    }

    public void parse(String cql) {
        try {

            CommonTokenStream tokens = getCommonTokenStream(cql);

            cqlParser parser = new cqlParser(tokens);
            //parser.addErrorListener(new AntlCqlParser.SyntaxErrorListener(v));
            parser.setBuildParseTree(true);
            cqlParser.LibraryContext tree = parser.library();

            // show tree in text form
            System.out.println(tree.toStringTree(parser));

            ConversionParser conversionParser = new ConversionParser(tokens);
            ParseTreeWalker walker = new ParseTreeWalker();
            walker.walk(conversionParser, tree);

        } catch (RuntimeException | IOException e) {
            log.warn("RuntimeException encountered in CqlParser", e);
            throw new CqlParseException(e.getMessage(), e);
        }
    }

    public CommonTokenStream getCommonTokenStream(String cql) throws IOException {
        InputStream stream = new ByteArrayInputStream(cql.getBytes());
        cqlLexer lexer = new cqlLexer(new ANTLRInputStream(stream));
        return new CommonTokenStream(lexer);
    }

    @Builder
    @Getter
    static class DefineStatementData {
        String qdmType;
        String fhirType;
        String code;
        String alias;
    }

    class ConversionParser extends cqlBaseListener {

        private static final String CANNOT_FIND_FHIR_TYPE_ERROR = "Cannot find a fhir type for qdmType: %s";
        private static final String CANNOT_FIND_FHIR_ATTRIBUTE_ERROR = "Cannot find a fhir attribute for qdmType: %s " +
                "qdmAttribute: %s fhirType: %s";

        private final CommonTokenStream tokens;
        List<DefineStatementData> statementList = new ArrayList<>();
        List<String> errors = new ArrayList<>();


        ConversionParser(CommonTokenStream tokens) {
            this.tokens = tokens;
        }

        @Override
        public void enterStatement(cqlParser.StatementContext ctx) {

            String original = getOriginalText(ctx);
            log.debug(original);

            int start = ctx.start.getTokenIndex();
            int stop = ctx.stop.getTokenIndex();


            StringBuilder stringBuilder = new StringBuilder();


            for (int y = start; y <= stop; y++) {
                Token token = tokens.get(y);
                log.debug(token.getTokenIndex() + ") " + token.getType() + "->" + token.getText());

                if (token.getType() == QUOTEDIDENTIFIER) {
                    if (token.getText().startsWith("\"SDE ")) {
                        stringBuilder = new StringBuilder(original); // clear
                        break; // SDE leave alone
                    }

                    String value = handleQuotedToken(token);
                    stringBuilder.append(value);
                } else if (token.getType() == IDENTIFIER) {
                    String value = handleIdentifier(token);
                    stringBuilder.append(value);
                } else {
                    stringBuilder.append(token.getText());
                }
            }

            log.debug(stringBuilder.toString());
        }

        private String handleIdentifier(Token token) {
            try {
                if (!checkTokenType(token.getTokenIndex() - 1, DOT)) {
                    return token.getText();
                }

                return processIdentifier(token);
            } catch (TokenException e) {
                return token.getText();
            }

        }

        Optional<DefineStatementData> findDefineStatementData(String alias) {
            return statementList.stream()
                    .filter(d -> d.getAlias().equals(alias))
                    .findFirst();

        }

        private String processIdentifier(Token attributeToken) {
            Token aliasToken = verifyTokenType(attributeToken.getTokenIndex() - 2, IDENTIFIER);

            var defineStatementDataOptional = findDefineStatementData(aliasToken.getText());

            if (defineStatementDataOptional.isEmpty()) {
                return attributeToken.getText();
            } else {
                DefineStatementData defineStatementData = defineStatementDataOptional.get();

                var fhirPropertyOptional = conversionDataComponent.findFhirAttribute(defineStatementData.getQdmType(),
                        attributeToken.getText(),
                        defineStatementData.getFhirType());

                if (fhirPropertyOptional.isPresent()) {
                    return fhirPropertyOptional.get();
                } else {
                    String errorMessage = String.format(CANNOT_FIND_FHIR_ATTRIBUTE_ERROR,
                            defineStatementData.getQdmType(),
                            attributeToken.getText(),
                            defineStatementData.getFhirType());
                    log.warn(errorMessage);
                    errors.add(errorMessage);
                    return attributeToken.getText();
                }
            }

        }

        private String handleQuotedToken(Token token) {
            try {
                if (!checkTokenType(token.getTokenIndex() - 1, OPEN_BRACKET)) {
                    return token.getText();
                }

                return processQuotedToken(token);
            } catch (TokenException e) {
                return token.getText();
            }
        }

        private String processQuotedToken(Token qdmTypeToken) {
            var optionalFhirType = conversionDataComponent.findFhirType(qdmTypeToken.getText());

            String fhirType = null;

            if (optionalFhirType.isPresent()) {
                fhirType = '"' + optionalFhirType.get() + '"';
            } else {
                String errorMessage = String.format(CANNOT_FIND_FHIR_TYPE_ERROR, qdmTypeToken.getText());
                log.warn(errorMessage);
                errors.add(errorMessage);
                return qdmTypeToken.getText();
            }

            verifyTokenType(qdmTypeToken.getTokenIndex() + 1, COLON);
            verifyTokenType(qdmTypeToken.getTokenIndex() + 2, WS);
            verifyTokenType(qdmTypeToken.getTokenIndex() + 3, QUOTEDIDENTIFIER);

            Token codeToken = verifyTokenType(qdmTypeToken.getTokenIndex() + 3, QUOTEDIDENTIFIER);

            verifyTokenType(qdmTypeToken.getTokenIndex() + 4, CLOSED_BRACKET);
            verifyTokenType(qdmTypeToken.getTokenIndex() + 5, WS);

            Token aliasToken = verifyTokenType(qdmTypeToken.getTokenIndex() + 6, IDENTIFIER);

            DefineStatementData defineStatementData = DefineStatementData.builder()
                    .fhirType(fhirType)
                    .qdmType(qdmTypeToken.getText())
                    .code(codeToken.getText())
                    .alias(aliasToken.getText())
                    .build();
            statementList.add(defineStatementData);

            return fhirType != null ? fhirType : qdmTypeToken.getText();
        }


        private Token verifyTokenType(int index, int expectedType) {
            Token token = tokens.get(index);

            if (token.getType() != expectedType) {
                throw new TokenException(index, expectedType, token.getType());
            } else {
                log.info("Token verified at index: {} type: {}", index, token.getType());
                return token;
            }
        }

        private boolean checkTokenType(int index, int expectedType) {
            return tokens.get(index).getType() == expectedType;
        }


        public String getOriginalText(ParserRuleContext ctx) {
            Interval interval = new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
            return ctx.start.getInputStream().getText(interval);
        }

        /**
         * {@inheritDoc}
         *
         * <p>The default implementation does nothing.</p>
         */
        @Override
        public void exitFunctionDefinition(cqlParser.FunctionDefinitionContext ctx) {
            log.debug("EXIT");
            statementList.clear();
            errors.clear();
        }
    }

}
