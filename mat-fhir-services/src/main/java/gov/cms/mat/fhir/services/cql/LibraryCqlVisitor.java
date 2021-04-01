package gov.cms.mat.fhir.services.cql;

import gov.cms.mat.fhir.commons.model.HumanReadableArtifacts;
import gov.cms.mat.fhir.commons.model.HumanReadableCodeModel;
import gov.cms.mat.fhir.commons.model.HumanReadableTypeModel;
import gov.cms.mat.fhir.commons.model.HumanReadableValuesetModel;
import gov.cms.mat.fhir.services.cql.parser.CqlUtils;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.cqframework.cql.gen.cqlBaseVisitor;
import org.cqframework.cql.gen.cqlParser;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DataRequirement;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.RelatedArtifact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Useful for parsing out code systems, value sets, data requirements, related Artifacts, libraries.
 */
@Getter
@Slf4j
public class LibraryCqlVisitor extends cqlBaseVisitor<String> {
    private static final String MISSING_INCLUDE_TEMPLATE = "Could not find included library: {} for parent library {}v{}";
    private final HapiFhirServer hapiServer;
    private final CQLAntlrUtils cqlAntlrUtils;
    private final LibraryCqlVisitorFactory factory;
    private final List<cqlParser.IncludeDefinitionContext> includes = new ArrayList<>();
    private final List<cqlParser.ValuesetDefinitionContext> valueSets = new ArrayList<>();
    private final List<cqlParser.CodeDefinitionContext> codes = new ArrayList<>();
    private final List<cqlParser.CodesystemDefinitionContext> codeSystems = new ArrayList<>();
    private final HumanReadableArtifacts humanReadableArtifacts = new HumanReadableArtifacts();
    private final List<DataRequirement> dataRequirements = new ArrayList<>();
    private final List<RelatedArtifact> relatedArtifacts = new ArrayList<>();
    private final Map<String, Pair<Library, LibraryCqlVisitor>> libMap = new HashMap<>();
    private final String matFhirBaseUrl;
    private final Map<Integer, Library> libraryCacheMap = new HashMap<>();
    private String name;
    private String version;

    public LibraryCqlVisitor(HapiFhirServer hapiServer,
                             CQLAntlrUtils cqlAntlrUtils,
                             LibraryCqlVisitorFactory factory,
                             String matFhirBaseUrl) {
        this.hapiServer = hapiServer;
        this.cqlAntlrUtils = cqlAntlrUtils;
        this.factory = factory;
        this.matFhirBaseUrl = matFhirBaseUrl;
    }

    /**
     * Stores off lib name and version.
     *
     * @param ctx The context.
     * @return Always null.
     */
    @Override
    public String visitLibraryDefinition(cqlParser.LibraryDefinitionContext ctx) {
        name = ctx.qualifiedIdentifier().getText();
        version = trim1(ctx.versionSpecifier().getText());
        return null;
    }

    /**
     * Stores off valueSets ,they are used later when looking up retrieves.
     *
     * @param ctx The context.
     * @return Always null.
     */
    @Override
    public String visitValuesetDefinition(cqlParser.ValuesetDefinitionContext ctx) {
        String name = getUnquotedFullText(ctx.identifier());
        String uri = getUnquotedFullText(ctx.valuesetId());
        valueSets.add(ctx);
        humanReadableArtifacts.getTerminologyValueSetModels().add(new HumanReadableValuesetModel(name,
                uri,
                null,
                null));

        RelatedArtifact relatedArtifact = new RelatedArtifact();
        relatedArtifact.setType(RelatedArtifact.RelatedArtifactType.DEPENDSON);
        relatedArtifact.setUrl(uri);
        relatedArtifacts.add(relatedArtifact);

        return null;
    }

    /**
     * Stores off all includes ,they are used later when looking for retrieves,
     * and builds relatedArtifacts for them
     *
     * @param ctx The context.
     * @return Always null.
     */
    @Override
    public String visitIncludeDefinition(cqlParser.IncludeDefinitionContext ctx) {
        if (ctx.getChildCount() >= 4 &&
                StringUtils.equals(ctx.getChild(0).getText(), "include") &&
                StringUtils.equals(ctx.getChild(2).getText(), "version")) {
            RelatedArtifact relatedArtifact = new RelatedArtifact();
            relatedArtifact.setType(RelatedArtifact.RelatedArtifactType.DEPENDSON);
            var nameVersion = getNameVersionFromInclude(ctx);

            Optional<Library> lib = fetchOptionalLibraryWithCacheMap(nameVersion);

            if (lib.isPresent()) {
                String identifier = ctx.localIdentifier().getText();
                Library childLib = lib.get();
                relatedArtifact.setUrl(matFhirBaseUrl + "/Library/" + nameVersion.getLeft());

                String cql = cqlAntlrUtils.getCql(childLib);
                LibraryCqlVisitor visitor = factory.visit(cql);
                libMap.put(identifier, Pair.of(childLib, visitor));
                relatedArtifacts.add(relatedArtifact);
            } else {
                log.error(MISSING_INCLUDE_TEMPLATE, nameVersion, name, version);
            }
            includes.add(ctx);
        }
        return null;
    }

    private Optional<Library> fetchOptionalLibraryWithCacheMap(Pair<String, String> nameVersion) {
        int hash = nameVersion.hashCode();

        if (libraryCacheMap.containsKey(hash)) {
            return Optional.of(libraryCacheMap.get(hash));
        } else {
            var optional = hapiServer.fetchHapiLibrary(nameVersion.getLeft(), nameVersion.getRight());

            optional.ifPresent(library -> libraryCacheMap.put(hash, library));

            return optional;
        }
    }

    @Override
    public String visitCodesystemDefinition(cqlParser.CodesystemDefinitionContext ctx) {
        codeSystems.add(ctx);
        RelatedArtifact relatedArtifact = new RelatedArtifact();
        relatedArtifact.setType(RelatedArtifact.RelatedArtifactType.DEPENDSON);
        relatedArtifact.setUrl(getUnquotedFullText(ctx.codesystemId()) +
                (ctx.versionSpecifier() != null ? "|" + getUnquotedFullText(ctx.versionSpecifier()) : ""));
        relatedArtifacts.add(relatedArtifact);
        return null;
    }

    @Override
    public String visitCodeDefinition(cqlParser.CodeDefinitionContext ctx) {
        String name = getUnquotedFullText(ctx.identifier());
        String code = getUnquotedFullText(ctx.codeId());
        String codeSystemName = getUnquotedFullText(ctx.codesystemIdentifier());
        codes.add(ctx);

        codeSystems.stream()
                .filter(cs -> StringUtils.equals(getUnquotedFullText(cs.identifier()), codeSystemName))
                .findFirst().ifPresentOrElse(cs -> {
            String csName = getUnquotedFullText(cs.identifier());
            String csUri = getUnquotedFullText(cs.codesystemId());
            String csVersionUri = getUnquotedFullText(cs.versionSpecifier());
            humanReadableArtifacts.getTerminologyCodeModels().add(HumanReadableCodeModel.builder()
                    .name(name)
                    .oid(code)
                    .codeSystemOid(csUri)
                    .codesystemName(csName)
                    .codesystemVersion(csVersionUri)
                    .isCodesystemVersionIncluded(csVersionUri != null)
                    .datatype(null)
                    .build());
        }, () -> log.error("Invalid code " + ctx.getText() + ". Could not find code system name " + codeSystemName));
        return null;
    }

    /**
     * Handles building the dataRequirements from retrieves.
     *
     * @param ctx The context.
     * @return Always null.
     */
    @Override
    public String visitRetrieve(cqlParser.RetrieveContext ctx) {
        if (matchesPathRetrieve(ctx)) {
            handleDataRequirement(trimQuotes(ctx.getChild(1).getText()),
                    ctx.getChild(3).getText(),
                    trimQuotes(ctx.getChild(5).getText()));

        } else if (matchesNonPathRetrieve(ctx)) {
            handleDataRequirement(trimQuotes(ctx.getChild(1).getText()),
                    "code",
                    trimQuotes(ctx.getChild(3).getText()));
        } else if (matchesTypeRetrieve(ctx)) {
            handleTypeDataRequirement(trimQuotes(ctx.getChild(1).getText()));
            log.debug("Added type retrieve: " + ctx.getText());
        }

        return null;
    }

    public HumanReadableCodeModel getCode(String codeName) {
        HumanReadableCodeModel result = null;
        if (isInIncludeLib(codeName)) {
            var optionalCm = humanReadableArtifacts.getTerminologyCodeModels().stream().filter(cm -> StringUtils.equals(cm.getName(), codeName)).findFirst();
            if (optionalCm.isPresent()) {
                result = optionalCm.get();
            } else {
                log.error("Could not find code with code name " + codeName);
            }
        } else {
            // Check to see if its a lib reference like TJC."value set id"
            int periodIndex = codeName.indexOf(".");
            if (periodIndex != -1) {
                String alias = codeName.substring(0, periodIndex);
                String remaining = codeName.substring(periodIndex + 1);
                var childLib = libMap.get(alias);
                if (childLib != null) {
                    result = childLib.getRight().getCode(remaining);
                }
            }
        }
        return result;
    }

    public HumanReadableValuesetModel getValueSetUrl(String valueSetName) {
        HumanReadableValuesetModel result = null;
        if (isInIncludeLib(valueSetName)) {
            var optionalVs = humanReadableArtifacts.getTerminologyValueSetModels().stream().filter(
                    vs -> StringUtils.equals(vs.getName(), valueSetName)).findFirst();
            if (optionalVs.isPresent()) {
                result = optionalVs.get();
            } else {
                log.debug("Could not find valueset with name " + valueSetName);
            }
        } else {
            // Check to see if its a lib reference like TJC."value set id"
            int periodIndex = valueSetName.indexOf(".");
            if (periodIndex != -1) {
                String alias = valueSetName.substring(0, periodIndex);
                String remaining = trimQuotes(valueSetName.substring(periodIndex + 1));
                var childLib = libMap.get(alias);
                if (childLib != null) {
                    result = childLib.getRight().getValueSetUrl(remaining);
                }
            }
        }
        return result;
    }

    private Pair<String, String> getNameVersionFromInclude(cqlParser.IncludeDefinitionContext ctx) {
        return Pair.of(ctx.getChild(1).getText(), trim1(ctx.getChild(3).getText()));
    }

    /**
     * @return Has to be something so always null.
     */
    @Override
    protected String defaultResult() {
        return null;
    }

    /**
     * @param ctx the retrieve.
     * @return Returns true if the retrieve is a valueset retrieve.
     */
    private boolean matchesTypeRetrieve(cqlParser.RetrieveContext ctx) {
        // define FirstInpatientEncounter:
        //   First([Encounter] E where E.class = 'inpatient' sort by period.start desc)
        return ctx.getChildCount() == 3 &&
                ctx.getChild(0).getText().equals("[") &&
                ctx.getChild(2).getText().equals("]");
    }

    /**
     * @param ctx the retrieve.
     * @return Returns true if the retrieve is a valueset retrieve.
     */
    private boolean matchesPathRetrieve(cqlParser.RetrieveContext ctx) {
        return ctx.getChildCount() == 7 &&
                ctx.getChild(0).getText().equals("[") &&
                ctx.getChild(2).getText().equals(":") &&
                ctx.getChild(4).getText().equals("in") &&
                ctx.getChild(6).getText().equals("]");
    }

    /**
     * @param ctx the retrieve.
     * @return Returns true if the retrieve is a valueset retrieve.
     */
    private boolean matchesNonPathRetrieve(cqlParser.RetrieveContext ctx) {
        return ctx.getChildCount() == 5 &&
                ctx.getChild(0).getText().equals("[") &&
                ctx.getChild(2).getText().equals(":") &&
                ctx.getChild(4).getText().equals("]");
    }

    private void handleTypeDataRequirement(String type) {
        // define FirstInpatientEncounter:
        //   First([Encounter] E where E.class = 'inpatient' sort by period.start desc)
        var result = new DataRequirement();
        result.setType(type);
        dataRequirements.add(result);
        humanReadableArtifacts.getDataReqTypes().add(HumanReadableTypeModel.builder()
                .type(type).build());
    }

    private void handleDataRequirement(String type, String path, String valueSetOrCodeName) {
        var result = new DataRequirement();
        result.setType(type);
        var filter = new DataRequirement.DataRequirementCodeFilterComponent();
        filter.setPath(path);
        result.setCodeFilter(Collections.singletonList(filter));

        var hrVs = getValueSetUrl(valueSetOrCodeName);
        if (hrVs != null) {
            filter.setValueSet(hrVs.getOid());
            humanReadableArtifacts.getDataReqValueSets().add(new HumanReadableValuesetModel(valueSetOrCodeName,
                    hrVs.getOid(),
                    hrVs.getVersion(),
                    type));
        } else {
            HumanReadableCodeModel hrCode = getCode(valueSetOrCodeName);
            if (hrCode != null) {
                filter.setCode(Collections.singletonList(new Coding(hrCode.getCodeSystemOid(), hrCode.getOid(), hrCode.getName())));
                humanReadableArtifacts.getDataReqCodes().add(HumanReadableCodeModel.builder()
                        .name(hrCode.getName())
                        .oid(hrCode.getOid())
                        .codeSystemOid(hrCode.getCodeSystemOid())
                        .codesystemName(hrCode.getCodesystemName())
                        .codesystemVersion(hrCode.getCodesystemVersion())
                        .isCodesystemVersionIncluded(hrCode.isCodesystemVersionIncluded())
                        .datatype(type)
                        .build());
            } else {
                log.info("Could not find a value set or code matching name " + valueSetOrCodeName + ". " +
                        "This is likely a FHIR type with attribute and we don't know how to handle those yet :(.");
            }
        }
        dataRequirements.add(result);
    }

    private String trimQuotes(String s) {
        if (StringUtils.startsWith(s, "\"") && StringUtils.endsWith(s, "\"")) {
            return trim1(s);
        } else {
            return s;
        }
    }

    /**
     * @param name The value set local id.
     * @return Returns true if the valueSetId is in this library.
     * False if it is an included library of this one.
     */
    private boolean isInIncludeLib(String name) {
        return !name.contains(".");
    }

    private String trim1(String s) {
        if (StringUtils.isNotBlank(s) && s.length() > 2) {
            return s.substring(1, s.length() - 1);
        } else {
            return s;
        }
    }

    private String getUnquotedFullText(ParserRuleContext context) {
        return CqlUtils.unquote(getFullText(context));
    }

    private String getFullText(ParserRuleContext context) {
        return context == null ? null : context.getText();
    }
}