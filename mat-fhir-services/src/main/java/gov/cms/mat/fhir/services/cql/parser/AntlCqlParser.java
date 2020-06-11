package gov.cms.mat.fhir.services.cql.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.cqframework.cql.gen.cqlBaseListener;
import org.cqframework.cql.gen.cqlLexer;
import org.cqframework.cql.gen.cqlParser;
import org.springframework.stereotype.Service;

import gov.cms.mat.cql.elements.BaseProperties;
import gov.cms.mat.fhir.services.exceptions.CqlParseException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import mat.server.CQLUtilityClass;
import mat.shared.CQLError;

@Slf4j
@Service
public class AntlCqlParser implements CqlParser {

    public AntlCqlParser() {
    }

    public void parse(String cql, CqlVisitor v) {
        try {
            v.validateBeforeParse();

            InputStream stream = new ByteArrayInputStream(cql.getBytes());
            cqlLexer lexer = new cqlLexer(new ANTLRInputStream(stream));
            CommonTokenStream tokens = prepareTokens(lexer);
            cqlParser parser = prepareParser(v, tokens);
            AntlrCqlParserListener antlrCqlParserListener = new AntlrCqlParserListener(v, tokens);
            walkCqlSyntaxTree(parser, antlrCqlParserListener);

            v.validateAfterParse();
        } catch (RuntimeException | IOException e) {
            log.warn("RuntimeException encountered in CqlParser", e);
            throw new CqlParseException(e.getMessage(), e);
        }
    }

    private void walkCqlSyntaxTree(cqlParser parser, AntlrCqlParserListener antlrCqlParserListener) {
        ParseTree tree = parser.library();
        ParseTreeWalker walker = new ParseTreeWalker();

        walker.walk(antlrCqlParserListener, tree);
    }

    private cqlParser prepareParser(CqlVisitor v, CommonTokenStream tokens) {
        cqlParser parser = new cqlParser(tokens);
        parser.addErrorListener(new SyntaxErrorListener(v));
        parser.setBuildParseTree(true);
        return parser;
    }

    private CommonTokenStream prepareTokens(cqlLexer lexer) {
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        tokens.fill();
        return tokens;
    }

    @AllArgsConstructor
    @Data
    private static class AntlrCqlParserListener extends cqlBaseListener {

        private static final String DEFINE = "define";
        private static final String CONTEXT = "context";
        private static final String PARAMETER = "parameter";
        private CqlVisitor visitor;
        private CommonTokenStream tokens;

        @Override
        public void enterContextDefinition(cqlParser.ContextDefinitionContext ctx) {
            visitor.context(getFullText(ctx.identifier()));
        }

        @Override
        public void enterCodeDefinition(cqlParser.CodeDefinitionContext ctx) {
            String name = getUnquotedFullText(ctx.identifier());
            String code = getUnquotedFullText(ctx.codeId());
            String codeSystemName = getUnquotedFullText(ctx.codesystemIdentifier());
            String displayName = getCodeDefinitionDisplayName(ctx);
            visitor.code(name, code, codeSystemName, displayName);
        }

        private String getCodeDefinitionDisplayName(cqlParser.CodeDefinitionContext ctx) {
            String displayName = null;
            cqlParser.DisplayClauseContext displayClauseContext = ctx.displayClause();
            if (displayClauseContext != null && displayClauseContext.children.size() > 1) {
                displayName = CqlUtils.unquote(displayClauseContext.children.get(1).getText());
            }
            return displayName;
        }

        @Override
        public void enterCodesystemDefinition(cqlParser.CodesystemDefinitionContext ctx) {
            String name = getUnquotedFullText(ctx.identifier());
            String uri = getUnquotedFullText(ctx.codesystemId());
            String versionUri = getUnquotedFullText(ctx.versionSpecifier());
            visitor.codeSystem(name, uri, versionUri);
        }

        @Override
        public void enterLibraryDefinition(cqlParser.LibraryDefinitionContext ctx) {
            visitor.libraryTag(getFullText(ctx.qualifiedIdentifier()), getUnquotedFullText(ctx.versionSpecifier()));
        }

        @Override
        public void enterIncludeDefinition(cqlParser.IncludeDefinitionContext ctx) {
            String libName = getFullText(ctx.qualifiedIdentifier());
            String version = CqlUtils.unquote(getFullText(ctx.versionSpecifier()));
            String alias = getFullText(ctx.localIdentifier());
            String model = BaseProperties.LIBRARY_FHIR_TYPE;
            String modelVersion = BaseProperties.LIBRARY_FHIR_VERSION;
            visitor.includeLib(libName, version, alias, model, modelVersion);
        }

        @Override
        public void enterUsingDefinition(cqlParser.UsingDefinitionContext ctx) {
            visitor.usingModelVersionTag(getUnquotedFullText(ctx.modelIdentifier()), getUnquotedFullText(ctx.versionSpecifier()));
        }

        @Override
        public void enterValuesetDefinition(cqlParser.ValuesetDefinitionContext ctx) {
            String type = getUnquotedFullText(ctx.identifier());
            String uri = getUnquotedFullText(ctx.valuesetId());
            visitor.valueSet(type, uri);
        }

        @Override
        public void enterExpressionDefinition(cqlParser.ExpressionDefinitionContext ctx) {
            String identifier = CqlUtils.unquote(getFullText(ctx.identifier()));
            String logic = getDefinitionAndFunctionLogic(ctx).trim();
            String comment = getExpressionComment(ctx).trim();

            visitor.definition(identifier, logic, comment);
        }

        @Override
        public void enterFunctionDefinition(cqlParser.FunctionDefinitionContext ctx) {
            String identifier = CqlUtils.unquote(getFullText(ctx.identifierOrFunctionIdentifier()));
            String logic = CQLUtilityClass.replaceFirstWhitespaceInLineForExpression(getDefinitionAndFunctionLogic(ctx).trim());
            String comment = getExpressionComment(ctx).trim();

            List<FunctionArgument> functionArguments = new ArrayList<>();
            if (ctx.operandDefinition() != null) {
                for (cqlParser.OperandDefinitionContext operand : ctx.operandDefinition()) {
                    String name = "";
                    String type = "";
                    if (operand.referentialIdentifier() != null) {
                        name = getFullText(operand.referentialIdentifier());
                    }

                    if (operand.typeSpecifier() != null) {
                        type = getFullText(operand.typeSpecifier());
                    }

                    FunctionArgument functionArgument = new FunctionArgument();
                    functionArgument.setName(name);
                    functionArgument.setType(type);

                    functionArguments.add(functionArgument);
                }
            }

            visitor.function(identifier, functionArguments, logic, comment);
        }

        @Override
        public void enterParameterDefinition(cqlParser.ParameterDefinitionContext ctx) {
            String identifier = CqlUtils.unquote(getFullText(ctx.identifier()));
            String comment = getExpressionComment(ctx).trim();
            String logic = getParameterLogic(ctx, getFullText(ctx.identifier())).trim();

            visitor.parameter(identifier, logic, comment);
        }


        private String getUnquotedFullText(ParserRuleContext context) {
            return CqlUtils.unquote(getFullText(context));
        }

        private String getFullText(ParserRuleContext context) {
            if (context == null) {
                return null;
            }
            if (context.start == null || context.stop == null || context.start.getStartIndex() < 0 || context.stop.getStopIndex() < 0) {
                return context.getText();
            }
            return context.start.getInputStream().getText(Interval.of(context.start.getStartIndex(), context.stop.getStopIndex()));
        }

        private String getExpressionComment(ParserRuleContext ctx) {
            Token previous = tokens.get(ctx.start.getTokenIndex() - 2);
            if (previous.getType() == cqlLexer.COMMENT) {
                String comment = previous.getText();
                return trimComment(comment);
            }

            return "";
        }

        private String trimComment(String comment) {
            return comment.replace(CqlUtils.BLOCK_COMMENT_START, "").replace(CqlUtils.BLOCK_COMMENT_END, "").trim();
        }

        private String getDefinitionAndFunctionLogic(ParserRuleContext ctx) {
            return getTextBetweenTokenIndexes(ctx.start.getTokenIndex(), findExpressionLogicStop(ctx));
        }

        private String getTextBetweenTokenIndexes(int startTokenIndex, int stopTokenIndex) {
            List<Token> ts = tokens.getTokens(startTokenIndex, stopTokenIndex);

            boolean startAdding = false;
            String logic = "";
            for (Token t : ts) {
                if (startAdding) {
                    logic += t.getText();
                }

                if (t.getText().equals(":")) {
                    startAdding = true;
                }
            }
            return logic;
        }

        private String getParameterLogic(cqlParser.ParameterDefinitionContext ctx, String identifier) {
            List<Token> ts = tokens.getTokens(ctx.start.getTokenIndex(), findExpressionLogicStop(ctx));
            StringBuilder builder = new StringBuilder();
            for (Token t : ts) {
                builder.append(t.getText());
            }

            return builder.toString().replaceFirst(PARAMETER, "").replace("public", "").replace("private", "").replace(identifier, "").trim();
        }

        /**
         * A definition or function body should be considered done when it reaches the next define statement
         * or it reaches a comment for the next expression.
         *
         * @param ctx the context to find the end of the body of
         * @return the index of the last token in the body
         */
        private int findExpressionLogicStop(ParserRuleContext ctx) {
            int index = tokens.size() - 1; // Initialize to the last token
            List<Token> ts = tokens.getTokens(ctx.start.getTokenIndex(), tokens.size() - 1);

            // find the next define statement
            boolean startAdding = false;
            for (Token t : ts) {
                if ((t.getText().equals(DEFINE) || t.getText().contentEquals(PARAMETER) || t.getText().equals(CONTEXT)) && startAdding) {
                    index = t.getTokenIndex();
                    break;
                }

                // wait until the first define or parameter
                if (t.getText().equals(DEFINE) || t.getText().equals(PARAMETER)) {
                    startAdding = true;
                }
            }


            if (tokens.get(index).getText().equals(CONTEXT)) {
                return index - 1;
            }

            Token twoTokensBeforeToken = tokens.get(index - 2);
            // check if the expression has a comment associated to it
            // if it does, return the token before it
            if (twoTokensBeforeToken.getType() == cqlLexer.COMMENT) {
                return twoTokensBeforeToken.getTokenIndex() - 1;
            } else {
                return index - 1;
            }
        }
    }

    @AllArgsConstructor
    @Data
    private static class SyntaxErrorListener extends BaseErrorListener {
        private CqlVisitor visitor;

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
                                String msg, RecognitionException e) {
            CQLError error = new CQLError();
            error.setSeverity("Severe");
            error.setErrorMessage(msg);
            error.setErrorInLine(line - 1);
            error.setErrorAtOffset(charPositionInLine);
            error.setStartErrorInLine(line - 1);
            error.setEndErrorInLine(line - 1);
            visitor.handleError(error);
        }
    }


}
