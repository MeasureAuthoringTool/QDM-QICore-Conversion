package gov.cms.mat.fhir.services.cql.parser;

import gov.cms.mat.cql.elements.BaseProperties;
import gov.cms.mat.fhir.services.exceptions.CqlParseException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import mat.server.CQLUtilityClass;
import mat.shared.CQLError;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.cqframework.cql.gen.cqlBaseListener;
import org.cqframework.cql.gen.cqlLexer;
import org.cqframework.cql.gen.cqlParser;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class AntlCqlParser implements CqlParser {


    public AntlCqlParser() {
    }

    public void parse(String cql, CqlVisitor v) {
        try {
            v.validateBeforeParse(); // Creates new CQLModel obj if null.

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
        private static final String CODESYSTEM = "codesystem";
        private static final String CODE = "code";
        private static final String VALUESET = "valueset";

        private static final List<String> defineFunctionStopTokens = Arrays.asList(DEFINE,PARAMETER,CONTEXT);
        private static final List<String> paramStopTokens = Arrays.asList(DEFINE,PARAMETER,CONTEXT,CODESYSTEM,VALUESET,CODE);

        private CqlVisitor visitor;
        private CommonTokenStream tokens;

        @Override
        public void enterContextDefinition(cqlParser.ContextDefinitionContext ctx) {
            visitor.context(getFullText(ctx.identifier()), getLine(ctx));
        }

        @Override
        public void enterCodeDefinition(cqlParser.CodeDefinitionContext ctx) {
            String name = getUnquotedFullText(ctx.identifier());
            String code = getUnquotedFullText(ctx.codeId());
            String codeSystemName = getUnquotedFullText(ctx.codesystemIdentifier());
            String displayName = getCodeDefinitionDisplayName(ctx);
            visitor.code(name,
                    code,
                    codeSystemName,
                    displayName,
                    getLine(ctx));
        }

        private String getCodeDefinitionDisplayName(cqlParser.CodeDefinitionContext ctx) {
            String displayName = null;
            cqlParser.DisplayClauseContext displayClauseContext = ctx.displayClause();
            if (displayClauseContext != null &&
                    displayClauseContext.children.size() > 1) {
                displayName = CqlUtils.unquote(displayClauseContext.children.get(1).getText());
            }
            return displayName;
        }

        @Override
        public void enterCodesystemDefinition(cqlParser.CodesystemDefinitionContext ctx) {
            String name = getUnquotedFullText(ctx.identifier());
            String uri = getUnquotedFullText(ctx.codesystemId());
            String versionUri = getUnquotedFullText(ctx.versionSpecifier());
            visitor.codeSystem(name,
                    uri,
                    versionUri,
                    getLine(ctx));
        }

        @Override
        public void enterLibraryDefinition(cqlParser.LibraryDefinitionContext ctx) {
            String comments = findLibraryComments(ctx.stop.getTokenIndex());

            visitor.libraryTag(getFullText(ctx.qualifiedIdentifier()),
                    getUnquotedFullText(ctx.versionSpecifier()),
                    comments,
                    getLine(ctx));
        }

        @Override
        public void enterIncludeDefinition(cqlParser.IncludeDefinitionContext ctx) {
            String libName = getFullText(ctx.qualifiedIdentifier());
            String version = CqlUtils.unquote(getFullText(ctx.versionSpecifier()));
            String alias = getFullText(ctx.localIdentifier());
            String model = BaseProperties.LIBRARY_FHIR_TYPE;
            String modelVersion = BaseProperties.LIBRARY_FHIR_VERSION;
            visitor.includeLib(libName, version, alias, model, modelVersion, getLine(ctx));
        }

        @Override
        public void enterUsingDefinition(cqlParser.UsingDefinitionContext ctx) {
            visitor.usingModelVersionTag(getUnquotedFullText(ctx.qualifiedIdentifier()),
                    getUnquotedFullText(ctx.versionSpecifier()),
                    getLine(ctx));
        }

        @Override
        public void enterValuesetDefinition(cqlParser.ValuesetDefinitionContext ctx) {
            String type = getUnquotedFullText(ctx.identifier());
            String uri = getUnquotedFullText(ctx.valuesetId());
            visitor.valueSet(type,
                    uri,
                    getLine(ctx));
        }

        @Override
        public void enterExpressionDefinition(cqlParser.ExpressionDefinitionContext ctx) {
            String identifier = CqlUtils.unquote(getFullText(ctx.identifier()));
            String logic = getDefinitionAndFunctionLogic(ctx).trim();
            String comment = getExpressionComment(ctx).trim();

            visitor.definition(identifier, logic, comment, getLine(ctx));
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

            visitor.function(identifier, functionArguments, logic, comment, getLine(ctx));
        }

        @Override
        public void enterParameterDefinition(cqlParser.ParameterDefinitionContext ctx) {
            String identifier = CqlUtils.unquote(getFullText(ctx.identifier()));
            String comment = getExpressionComment(ctx).trim();
            String logic = getParameterLogic(ctx, getFullText(ctx.identifier())).trim();

            visitor.parameter(identifier, logic, comment, getLine(ctx));
        }

        private int getLine(ParserRuleContext ctx) {
            return tokens.get(ctx.getSourceInterval().a).getLine();
        }


        private String findLibraryComments(int start) {
            List<Token> comments = tokens.getTokens(start, tokens.size() - 1);

            var optional = comments.stream()
                    .takeWhile(t -> t.getType() != 3) // type: 3, text: using
                    .filter(t -> t.getType() == cqlParser.COMMENT)
                    .findFirst();

            return optional.map(token -> trimComment(token.getText())).orElse(null);
        }


        private String getUnquotedFullText(ParserRuleContext context) {
            return CqlUtils.unquote(getFullText(context));
        }

        private String getFullText(ParserRuleContext context) {
            if (context == null) {
                return null;
            }
            if (context.start == null ||
                    context.stop == null ||
                    context.start.getStartIndex() < 0 ||
                    context.stop.getStopIndex() < 0) {
                return context.getText();
            }
            return context.start.getInputStream().getText(Interval.of(context.start.getStartIndex(), context.stop.getStopIndex()));
        }

        private String getExpressionComment(ParserRuleContext ctx) {
            int index = ctx.start.getTokenIndex() - 2;
            if (isValidIndex(index)) {
                var token = tokens.get(index);
                if (token.getType() == cqlLexer.COMMENT) {
                    String comment = token.getText();
                    return trimComment(comment);
                }
            }
            return "";
        }

        private boolean isValidIndex(int i) {
            return i >= 0;
        }

        private String trimComment(String comment) {
            return comment.replace(CqlUtils.BLOCK_COMMENT_START,
                    "").replace(CqlUtils.BLOCK_COMMENT_END,
                    "").trim();
        }

        private String getDefinitionAndFunctionLogic(ParserRuleContext ctx) {
            return getTextBetweenTokenIndexes(ctx.start.getTokenIndex(), findExpressionLogicStop(ctx,defineFunctionStopTokens));
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
            List<Token> ts = tokens.getTokens(ctx.start.getTokenIndex(), findExpressionLogicStop(ctx,paramStopTokens));
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
        private int findExpressionLogicStop(ParserRuleContext ctx, List<String> stopTokens) {
            int index = tokens.size() - 1; // Initialize to the last token
            List<Token> ts = tokens.getTokens(ctx.start.getTokenIndex(), tokens.size() - 1);

            // find the next define statement
            boolean startAdding = false;
            for (Token t : ts) {
                if (stopTokens.contains(t.getText()) && startAdding) {
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

            if (isValidIndex(index - 2)) {
                Token twoTokensBeforeToken = tokens.get(index - 2);
                // check if the expression has a comment associated to it
                // if it does, return the token before it
                if (twoTokensBeforeToken.getType() == cqlLexer.COMMENT) {
                    return twoTokensBeforeToken.getTokenIndex() - 1;
                }
            }
            return index - 1;
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
            error.setErrorInLine(line);
            error.setErrorAtOffset(charPositionInLine);
            error.setStartErrorInLine(line);
            error.setEndErrorInLine(line);
            visitor.handleError(error);
        }
    }
}
