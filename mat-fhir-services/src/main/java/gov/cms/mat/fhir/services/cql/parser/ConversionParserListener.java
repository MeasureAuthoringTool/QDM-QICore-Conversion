package gov.cms.mat.fhir.services.cql.parser;

import gov.cms.mat.fhir.services.components.conversion.ConversionDataComponent;
import gov.cms.mat.fhir.services.components.conversion.ConversionDataTypeResult;
import gov.cms.mat.fhir.services.components.conversion.ConversionFhirAttributeResult;
import gov.cms.mat.fhir.services.exceptions.CqlParseException;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.cqframework.cql.gen.cqlBaseListener;
import org.cqframework.cql.gen.cqlLexer;
import org.cqframework.cql.gen.cqlParser;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.cqframework.cql.gen.cqlLexer.IDENTIFIER;
import static org.cqframework.cql.gen.cqlLexer.QUOTEDIDENTIFIER;
import static org.cqframework.cql.gen.cqlLexer.WS;

@Slf4j
@Component
/**
 * https://docs.google.com/spreadsheets/d/1lV1N4O7xmSxjRH6ghuCj3mYcntPsTz8qBcpQ6DO7Se4/edit#gid=1682709111
 */
public class ConversionParserListener extends cqlBaseListener {
    private static final int COLON = 11;
    private static final int DOT = 17;
    private static final int OPEN_BRACKET = 38;
    private static final int CLOSED_BRACKET = 40;
    private static final int SORT = 51;
    private static final int WHERE = 45;
    private static final int UNION = 79;
    private final ConversionDataComponent conversionDataComponent;

    public ConversionParserListener(ConversionDataComponent conversionDataComponent) {
        this.conversionDataComponent = conversionDataComponent;
    }

    public String convert(String cql) {
        try {
            CommonTokenStream tokens = getCommonTokenStream(cql);

            cqlParser parser = new cqlParser(tokens);
            //parser.addErrorListener(new AntlCqlParser.SyntaxErrorListener(v));
            parser.setBuildParseTree(true);
            cqlParser.LibraryContext tree = parser.library();

            log.debug("ANTLR tree: {}", tree.toStringTree(parser));

            ConversionParser conversionParser = new ConversionParser(tokens);
            ParseTreeWalker walker = new ParseTreeWalker();
            walker.walk(conversionParser, tree);

            String convertedCql = cql;

            for (ConversionResultsData c : conversionParser.conversionResults) {
                convertedCql = convertedCql.replace(c.getOriginal(), c.getConverted());
            }

            return convertedCql;

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
    static class ConversionResultsData {
        String original;
        String converted;
    }

    @Builder
    @Getter
    static class ErrorData {
        String message;
        int count;

        void incrementCount() {
            count++;
        }
    }

    @Builder
    @Getter
    @EqualsAndHashCode
    static class UnionResultsData {
        String fhirType;
        String qdmType;
    }

    class ConversionParser extends cqlBaseListener {
        private static final String CANNOT_FIND_FHIR_TYPE_ERROR = "Cannot find a fhir type for qdmType: %s";

        private static final String CANNOT_FIND_FHIR_ATTRIBUTE_COMMENT = " //No FHIR mapping for '%s'";

        private static final String MISSING_WHERE_CLAUSE = " //Unable to convert negation without a where clause.";

        private static final String UNION_ISSUE = " //Unable to convert %s for mixed types.";

        final List<ConversionResultsData> conversionResults = new ArrayList<>();
        private final CommonTokenStream tokens;
        private final List<DefineStatementData> statementList = new ArrayList<>();
        private final List<String> errors = new ArrayList<>();

        private final List<DefineStatementData> unionList = new ArrayList<>();

        private final Map<String, ErrorData> errorMap = new HashMap<>();

        String originalCql;
        private String removal = "";
        private ConversionResultsData change;

        ConversionParser(CommonTokenStream tokens) {
            this.tokens = tokens;
        }

        @Override
        public void enterStatement(cqlParser.StatementContext ctx) {
            originalCql = getOriginalText(ctx);
            log.debug(originalCql);

            String convertedCql;

            try {
                convertedCql = processTokens(ctx);
            } catch (WhereClauseException e) {
                convertedCql = originalCql;
            }

            if (convertedCql.length() > 0) {

                boolean haveError = statementList.stream().anyMatch(s -> BooleanUtils.isFalse(s.hasWhereClause));

                if (haveError) {
                    convertedCql = originalCql;
                } else {

                    if (StringUtils.isNotEmpty(removal)) {
                        convertedCql = convertedCql.replace(removal, "");
                        removal = "";
                    }
                    convertedCql = processWhereAdjustments(convertedCql);
                }


                convertedCql = processError(convertedCql);


                if (change != null) {
                    convertedCql = convertedCql.replace(change.getOriginal(), change.getConverted());
                    change = null;
                }

                if (originalCql.equals(convertedCql)) {
                    log.debug("No changes to cql");
                } else {
                    createResults(convertedCql);
                }
            } else {
                log.debug("No buffer changes to cql were found");
            }

            statementList.clear();
        }


        private String processWhereAdjustments(String convertedCql) {
            final String whereTag = "where";

            for (DefineStatementData data : statementList) {
                if (StringUtils.isNotBlank(data.whereAdjustment)) {

                    List<String> lines = getLines(convertedCql);

                    for (String line : lines) {
                        if (line.strip().startsWith(whereTag)) {

                            int start = line.indexOf(whereTag) + whereTag.length();
                            String oldLogic = line.substring(start);

                            String whereLine = data.whereAdjustment.replace("alias.", data.getAlias() + ".");

                            convertedCql = convertedCql.replace(oldLogic,
                                    " " + whereLine + " and\n" + "      " + oldLogic + "\n");

                            break;
                        }
                    }
                }
            }

            return convertedCql;
        }

        public void createResults(String convertedCql) {
            if (!errors.isEmpty()) {
                for (String e : errors) {
                    convertedCql = "// Conversion Error: " + e + "\n" + convertedCql;
                }
            }

            log.debug("CQl changed converted results: {}", convertedCql);

            var result = ConversionResultsData.builder()
                    .original(originalCql)
                    .converted(convertedCql)
                    .build();

            conversionResults.add(result);
        }

        public String processError(String convertedCql) {
            if (!errorMap.isEmpty()) {

                List<String> lines = getLines(convertedCql);

                for (Map.Entry<String, ErrorData> entry : errorMap.entrySet()) {
                    int hits = 0;

                    for (String line : lines) {
                        if (line.contains(entry.getKey())) {
                            convertedCql = convertedCql.replace(line, line + entry.getValue().getMessage());

                            if (++hits >= entry.getValue().getCount()) {
                                break;
                            }
                        }
                    }
                }
            }

            return convertedCql;
        }

        public List<String> getLines(String convertedCql) {
            return new BufferedReader(new StringReader(convertedCql))
                    .lines()
                    .collect(Collectors.toList());
        }

        public String processTokens(cqlParser.StatementContext ctx) {
            StringBuilder stringBuilder = new StringBuilder();
            int start = ctx.start.getTokenIndex();
            int stop = ctx.stop.getTokenIndex();

            for (int y = start; y <= stop; y++) {
                Token token = tokens.get(y);
                log.debug(token.getTokenIndex() + ") " + token.getType() + "->" + token.getText());

                if (token.getType() == QUOTEDIDENTIFIER) {
                    if (token.getText().startsWith("\"SDE ")) {
                        stringBuilder = new StringBuilder(originalCql); // clear
                        break; // SDE leave alone
                    }

                    String value = handleQuotedToken(token);
                    stringBuilder.append(value);
                } else if (token.getType() == IDENTIFIER) {
                    if (checkTokenType(y - 1, WS)) {
                        String value = handleSort(token);
                        stringBuilder.append(value);
                    } else {
                        String value = handleIdentifier(token);
                        stringBuilder.append(value);
                    }
                } else {
                    stringBuilder.append(token.getText());
                }
            }

            return stringBuilder.toString();
        }

        private String handleSort(Token tokenIn) {
            boolean foundSort = false;

            for (int x = tokenIn.getTokenIndex() - 2; x > 0; x--) {
                Token token = tokens.get(x);

                if (token.getType() == SORT) {
                    foundSort = true;
                } else if (token.getType() == IDENTIFIER) {

                    if (foundSort) {
                        var defineStatementDataOptional = findDefineStatementData(token.getText());

                        if (defineStatementDataOptional.isEmpty()) {
                            return token.getText();
                        } else {
                            return findFhirType(tokenIn, defineStatementDataOptional.get());
                        }

                    } else {
                        break;
                    }
                }
            }

            return tokenIn.getText();
        }

        private String findFhirType(Token token, DefineStatementData defineStatementData) {
            var fhirPropertyOptional = conversionDataComponent.findFhirAttribute(defineStatementData.getQdmType(),
                    token.getText(),
                    defineStatementData.getFhirType());

            if (fhirPropertyOptional.isPresent()) {
                return fhirPropertyOptional.get().getType();
            } else {

                String type = defineStatementData.getQdmType() + '.' + token.getText();
                String message = String.format(CANNOT_FIND_FHIR_ATTRIBUTE_COMMENT, token.getText());
                addError(type, message);

                return token.getText();
            }
        }

        public void addError(String type, String message) {

            log.warn(message);

            var optional = errorMap.entrySet().stream()
                    .filter(entry -> entry.getKey().equals(type))
                    .filter(entry -> entry.getValue().getMessage().equals(message))
                    .map(Map.Entry::getValue)
                    .findFirst();

            if (optional.isPresent()) {
                optional.get().incrementCount();
            } else {
                errorMap.put(type,
                        ErrorData.builder().count(1).message(message).build());
            }

        }

        private String handleIdentifier(Token token) {
            try {
                if (!checkTokenType(token.getTokenIndex() - 1, DOT)) {
                    return token.getText();
                }

                // Study.relevantPeriod.high
                if (checkTokenType(token.getTokenIndex() + 1, DOT)) {
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
            int start = attributeToken.getTokenIndex() - 1;

            List<Token> params = new ArrayList<>();
            params.add(attributeToken);

            while (checkTokenType(start--, DOT)) {
                Token token = verifyTokenType(start--, IDENTIFIER);
                params.add(token);
            }

            String qdmAttribute = buildAttribute(params);

            Token aliasToken = params.stream().reduce((first, second) -> second)
                    .orElseThrow(() -> new TokenException("Cannot parse identifier: " + attributeToken.getText()));

            String ofType = "";

            if (checkTokenType(attributeToken.getTokenIndex() - (start - 1), WS) &&
                    checkTokenType(attributeToken.getTokenIndex() - (start - 2), 107) &&
                    checkTokenType(attributeToken.getTokenIndex() - (start - 3), WS)) {

                Token ofToken = verifyTokenType(start - 6, 105);

                ofType = ofToken.getText();
            }

            var defineStatementDataOptional = findDefineStatementData(aliasToken.getText());

            if (defineStatementDataOptional.isEmpty()) {

                var optionalText = conversionDataComponent.findCommentForMissingType(qdmAttribute);

                if (optionalText.isPresent()) {
                    addError(aliasToken.getText() + '.' + qdmAttribute, " //" + optionalText.get());
                }

                return attributeToken.getText();
            } else {
                DefineStatementData defineStatementData = defineStatementDataOptional.get();

                if (CollectionUtils.isNotEmpty(defineStatementData.getUnions())) {
                    if (!checkUnion(defineStatementData, qdmAttribute)) {
                        String type = aliasToken.getText() + '.' + qdmAttribute;
                        String message = String.format(UNION_ISSUE, qdmAttribute);

                        addError(type, message);

                        return attributeToken.getText();
                    }
                }

                if (StringUtils.isNotBlank(ofType)) {
                    qdmAttribute = qdmAttribute + " " + ofType;
                }

                var fhirPropertyOptional = conversionDataComponent.findFhirAttribute(defineStatementData.getQdmType(),
                        qdmAttribute,
                        defineStatementData.getFhirType());

                if (fhirPropertyOptional.isPresent()) {
                    if (StringUtils.isNotBlank(ofType)) {
                        removal = ofType + " of ";
                    }

                    String fhirProperty = fhirPropertyOptional.get().getType();

                    if (fhirProperty.startsWith("(alias.")) {
                        fhirProperty = fhirProperty.replace("alias.", aliasToken.getText() + ".");
                        String toReplace = aliasToken.getText() + "." + qdmAttribute;
                        change = ConversionResultsData.builder().original(toReplace).converted(fhirProperty).build();
                        return attributeToken.getText();
                    } else {
                        return fhirProperty;
                    }
                } else {
                    String type = aliasToken.getText() + '.' + qdmAttribute;
                    String message = String.format(CANNOT_FIND_FHIR_ATTRIBUTE_COMMENT, qdmAttribute);

                    addError(type, message);

                    return attributeToken.getText();
                }
            }

        }

        private boolean checkUnion(DefineStatementData defineStatementData, String qdmAttribute) {

            String lastResult = null;

            for (DefineStatementData.UnionData u : defineStatementData.getUnions()) {
                var fhirPropertyOptional = conversionDataComponent.findFhirAttribute(u.getQdmType(),
                        qdmAttribute,
                        u.getFhirType());

                if (fhirPropertyOptional.isPresent()) {
                    ConversionFhirAttributeResult result = fhirPropertyOptional.get();

                    if (lastResult != null && !lastResult.equals(result.getFhirAttribute())) {
                        return false;
                    } else {
                        lastResult = result.getFhirAttribute();
                    }
                }

            }

            return true;

        }

        private String buildAttribute(List<Token> params) {
            StringBuilder stringBuilder = new StringBuilder();

            for (int x = params.size() - 2; x >= 0; x--) {
                stringBuilder.append(params.get(x).getText());

                if (x - 1 >= 0) {
                    stringBuilder.append(".");
                }
            }

            return stringBuilder.toString();
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
            var optionalDataResult = conversionDataComponent.findFhirType(qdmTypeToken.getText());

            String fhirType;
            String whereAdjustment;

            if (optionalDataResult.isPresent()) {
                ConversionDataTypeResult result = optionalDataResult.get();
                whereAdjustment = result.getWhereAdjustment();

                if (StringUtils.isNotBlank(result.getComment())) {
                    addError(qdmTypeToken.getText(), " //" + result.getComment());
                }

                if (StringUtils.isBlank(result.getType())) {
                    return qdmTypeToken.getText();
                } else {
                    fhirType = '"' + result.getType() + '"';
                }
            } else {
                String errorMessage = String.format(CANNOT_FIND_FHIR_TYPE_ERROR, qdmTypeToken.getText());
                log.warn(errorMessage);
                errors.add(errorMessage);
                return qdmTypeToken.getText();
            }

            if (checkTokenType(qdmTypeToken.getTokenIndex() + 1, CLOSED_BRACKET)) {
                return processNoValueSetCodeSystem(qdmTypeToken);
            } else {

                verifyTokenType(qdmTypeToken.getTokenIndex() + 1, COLON);

                verifyTokenType(qdmTypeToken.getTokenIndex() + 2, WS);

                Token codeToken = verifyTokenType(qdmTypeToken.getTokenIndex() + 3, QUOTEDIDENTIFIER);

                verifyTokenType(qdmTypeToken.getTokenIndex() + 4, CLOSED_BRACKET);

                try {
                    verifyTokenType(qdmTypeToken.getTokenIndex() + 5, WS);

                    boolean closingUnion = false;
                    int aliasOffset = 6;

                    if (checkTokenType(qdmTypeToken.getTokenIndex() + 6, 32)) {
                        log.debug("me");
                        closingUnion = true;
                        aliasOffset = aliasOffset + 2;
                    }

                    Token aliasToken = verifyTokenType(qdmTypeToken.getTokenIndex() + aliasOffset, IDENTIFIER);

                    DefineStatementData defineStatementData = DefineStatementData.builder()
                            .fhirType(fhirType)
                            .qdmType(qdmTypeToken.getText())
                            .code(codeToken.getText())
                            .alias(aliasToken.getText())
                            .whereAdjustment(whereAdjustment)
                            .build();
                    statementList.add(defineStatementData);

                    if (closingUnion) {
                        defineStatementData.createUnionDataSet(unionList);
                    }


                    if (defineStatementData.isNegation()) {
                        if (!containsTokenType(aliasToken.getTokenIndex() + 2, WHERE)) {
                            addError(aliasToken.getText(), MISSING_WHERE_CLAUSE);
                            defineStatementData.setHasWhereClause(Boolean.FALSE);
                            throw new WhereClauseException(MISSING_WHERE_CLAUSE);
                        } else {
                            defineStatementData.setHasWhereClause(Boolean.TRUE);
                        }
                    }
                } catch (TokenException e) { // we dont have an alias
                    DefineStatementData defineStatementData = DefineStatementData.builder()
                            .fhirType(fhirType)
                            .qdmType(qdmTypeToken.getText())
                            .code(codeToken.getText())
                            .whereAdjustment(whereAdjustment)
                            .build();

                    if (containsTokenType(qdmTypeToken.getTokenIndex() + 4, WS) && containsTokenType(qdmTypeToken.getTokenIndex() + 5, UNION)) {
                        unionList.add(defineStatementData);
                    }

                    if (defineStatementData.isNegation()) {
                        if (!containsTokenType(qdmTypeToken.getTokenIndex() + 5, WHERE)) {
                            addError(fhirType, MISSING_WHERE_CLAUSE);
                            defineStatementData.setHasWhereClause(Boolean.FALSE);
                            throw new WhereClauseException(MISSING_WHERE_CLAUSE);
                        }
                    }

                }

                return fhirType;
            }
        }

        private String processNoValueSetCodeSystem(Token qdmTypeToken) {
            var optionalDataResult = conversionDataComponent.findFhirType(qdmTypeToken.getText());


            if (optionalDataResult.isPresent()) {
                ConversionDataTypeResult result = optionalDataResult.get();

                if (StringUtils.isNotBlank(result.getComment())) {
                    addError(qdmTypeToken.getText(), " //" + result.getComment());
                }

                if (StringUtils.isBlank(result.getType())) {
                    return qdmTypeToken.getText();
                } else {

                    return '"' + result.getType() + '"';
                }
            } else {
                String errorMessage = String.format(CANNOT_FIND_FHIR_TYPE_ERROR, qdmTypeToken.getText());
                log.warn(errorMessage);
                errors.add(errorMessage);
                return qdmTypeToken.getText();
            }
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

        private boolean containsTokenType(int start, int expectedType) {
            for (int x = start; x < tokens.size(); x++) {
                if (tokens.get(x).getType() == expectedType) {
                    return true;
                }
            }

            return false;
        }


        private boolean checkTokenType(int index, int expectedType) {
            return tokens.get(index).getType() == expectedType;
        }

        public String getOriginalText(ParserRuleContext ctx) {
            Interval interval = new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
            return ctx.start.getInputStream().getText(interval);
        }

        @Override
        public void exitStatement(cqlParser.StatementContext ctx) {
            statementList.clear();
            errors.clear();
            change = null;
            removal = "";
            errorMap.clear();
            unionList.clear();
        }
    }

}
