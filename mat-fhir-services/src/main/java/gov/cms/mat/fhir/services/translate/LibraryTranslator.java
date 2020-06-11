package gov.cms.mat.fhir.services.translate;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.services.cql.CQLAntlrUtils;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.repository.CqlLibraryRepository;
import gov.cms.mat.fhir.services.repository.MeasureExportRepository;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.cqframework.cql.gen.cqlBaseVisitor;
import org.cqframework.cql.gen.cqlParser;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.DataRequirement;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.r4.model.RelatedArtifact;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Getter
@Setter
@Service
public class LibraryTranslator extends TranslatorBase {
    public static final String CQL_CONTENT_TYPE = "text/cql";
    public static final String JSON_ELM_CONTENT_TYPE = "application/elm+json";
    public static final String XML_ELM_CONTENT_TYPE = "application/elm+xml";
    public static final String SYSTEM_TYPE = "http://hl7.org/fhir/codesystem-library-type.html";
    public static final String SYSTEM_CODE = "logic-library";

    private final HapiFhirServer hapiServer;
    private final CQLAntlrUtils cqlAntlrUtils;
    private final MeasureExportRepository measureExportRepo;
    private final CqlLibraryRepository libRepo;


    public LibraryTranslator(HapiFhirServer hapiServer,
                             CQLAntlrUtils cqlAntlrUtils,
                             CqlLibraryRepository libRepo,
                             MeasureExportRepository measureExportRepo) {
        this.hapiServer = hapiServer;
        this.cqlAntlrUtils = cqlAntlrUtils;
        this.libRepo = libRepo;
        this.measureExportRepo = measureExportRepo;
    }

    public Library translateToFhir(String libId, String cql, String elmXml, String elmJson) {
        //Used to parse the CQL and get various values.
        LibCqlVisitor visitor = buildVisitor(cql);

        Library result = new Library();
        result.setId(libId);
        result.setName(visitor.getName());
        result.setVersion(visitor.getVersion());
        result.setDate(new Date());
        result.setStatus(Enumerations.PublicationStatus.ACTIVE);
        result.setContent(createContent(elmJson, cql, elmXml));
        result.setType(createType(SYSTEM_TYPE, SYSTEM_CODE));
        result.setApprovalDate(new Date()); //TO DO: fix this.
        result.setUrl(publicHapiFhirUrl + "Library/" + libId);
        result.setDataRequirement(distinctDataRequirements(visitor.getDataRequirements()));
        result.setRelatedArtifact(distinctArtifacts(visitor.getRelatedArtifacts()));
        result.setText(findHumanReadable(libId));

        //TO DO: figure out how to handle this with logging.
        //ConversionReporter.setLibraryValidationLink(result.getUrl(), CREATED, uuid);
        return result;
    }

    /**
     * Creates a CqlVisitor and visits the cql.
     *
     * @param cql     The cql.
     * @return The visitor.
     */
    private LibCqlVisitor buildVisitor(String cql) {
        LibCqlVisitor visitor = new LibCqlVisitor(publicHapiFhirUrl, hapiServer, cqlAntlrUtils);
        cqlParser.LibraryContext ctx = cqlAntlrUtils.getLibraryContext(cql);
        visitor.visit(ctx);
        return visitor;
    }

    private Narrative findHumanReadable(String libId) {
        Narrative result = null;
        CqlLibrary lib = libRepo.getCqlLibraryById(libId);
        if (lib != null && StringUtils.isNotBlank(lib.getMeasureId())) {
            var exportOpt = measureExportRepo.findByMeasureId(lib.getMeasureId());
            if (exportOpt.isPresent()) {
                var export = exportOpt.get();
                if (export.getHumanReadable() != null) {
                    result = createNarrative(lib.getMeasureId(), export.getHumanReadable());
                }
            }
        }
        return result;
    }

    /**
     * @param elmJson elmJson String
     * @param cql     cql String
     * @param elmXml  elmXml String
     * @return The content element.
     */
    List<Attachment> createContent(String elmJson, String cql, String elmXml) {
        List<Attachment> attachments = new ArrayList<>(2);
        attachments.add(createAttachment(CQL_CONTENT_TYPE, cql.getBytes()));
        attachments.add(createAttachment(XML_ELM_CONTENT_TYPE, elmXml.getBytes()));
        attachments.add(createAttachment(JSON_ELM_CONTENT_TYPE, elmJson.getBytes()));
        return attachments;
    }

    private List<RelatedArtifact> distinctArtifacts(List<RelatedArtifact> artifacts) {
        List<RelatedArtifact> result = new ArrayList<>(artifacts.size());
        //Remove duplicates. I wish HapiFhir implemented equals. Today it is SadFhir for me.
        artifacts.forEach(a -> {
            if (result.stream().noneMatch(ar -> Objects.deepEquals(a, ar))) {
                result.add(a);
            }
        });
        return result;
    }

    private List<DataRequirement> distinctDataRequirements(List<DataRequirement> reqs) {
        List<DataRequirement> result = new ArrayList<>(reqs.size());
        //Remove duplicates. I wish HapiFhir implemented equals. Today it is SadFhir for me.
        //Object.deepEquals doesn't work at all on codeFilter for some reason.
        reqs.forEach(r -> {
            if (result.stream().noneMatch(rr -> StringUtils.equals(r.getType(), rr.getType()) &&
                    ((CollectionUtils.isEmpty(r.getCodeFilter()) && CollectionUtils.isEmpty(rr.getCodeFilter())) ||
                            (r.getCodeFilter().size() == rr.getCodeFilter().size() &&
                                    StringUtils.equals(r.getCodeFilter().get(0).getValueSet(), rr.getCodeFilter().get(0).getValueSet()) &&
                                    StringUtils.equals(r.getCodeFilter().get(0).getPath(), rr.getCodeFilter().get(0).getPath()))))) {
                result.add(r);
            }
        });
        return result;
    }

    @Getter
    @Slf4j
    private static class LibCqlVisitor extends cqlBaseVisitor<String> {
        private final String publicFhirBase;
        private final HapiFhirServer hapiServer;
        private final CQLAntlrUtils cqlAntlrUtils;
        private String name;
        private String version;
        private final List<cqlParser.IncludeDefinitionContext> includes = new ArrayList<>();
        private final List<cqlParser.ValuesetDefinitionContext> valueSets = new ArrayList<>();
        private final List<DataRequirement> dataRequirements = new ArrayList<>();
        private final List<RelatedArtifact> relatedArtifacts = new ArrayList<>();
        private final List<Library> libs = new ArrayList<>();

        private LibCqlVisitor(String publicFhirBase,
                              HapiFhirServer hapiServer,
                              CQLAntlrUtils cqlAntlrUtils) {
            this.publicFhirBase = publicFhirBase;
            this.hapiServer = hapiServer;
            this.cqlAntlrUtils = cqlAntlrUtils;
        }

        /**
         * Stores off lib name and version.
         *
         * @param ctx The context.
         * @return Always null.
         */
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
            valueSets.add(ctx);
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
                Optional<Library> lib = hapiServer.fetchHapiLibrary(nameVersion.getLeft(),
                        nameVersion.getRight());
                if (lib.isPresent()) {
                    relatedArtifact.setUrl(publicFhirBase + "Library/" + getId(lib.get()));
                    libs.add(lib.get());
                    relatedArtifacts.add(relatedArtifact);
                } else {
                    log.warn("Could not find hapi fhir lib: " + name + "|" + version);
                }
                includes.add(ctx);
            }
            return null;
        }

        private String getId(Library lib) {
            String id = lib.getId();
            int endIndex = id.indexOf("/_history");
            if (endIndex >= 0) {
                id = id.substring(0,endIndex);
            }
            int startIndex = id.lastIndexOf('/') + 1;
            return id.substring(startIndex);
        }

        private Pair<String, String> getNameVersionFromInclude(cqlParser.IncludeDefinitionContext ctx) {
            return Pair.of(ctx.getChild(1).getText(), trim1(ctx.getChild(3).getText()));
        }

        /**
         * Handles building the dataRequirements from retrieves.
         *
         * @param ctx The context.
         * @return Always null.
         */
        @Override
        public String visitRetrieve(cqlParser.RetrieveContext ctx) {
            if (matchesValuesetPathRetrieve(ctx)) {
                dataRequirements.add(fromValueSetPathRetrieve(ctx));
            } else if (matchesValuesetRetrieve(ctx)) {
                dataRequirements.add(fromValueSetRetrieve(ctx));
            }else if (matchesSimpleRetrieve(ctx)) {
                dataRequirements.add(fromSimpleRetrieve(ctx));
            }
            return null;
        }

        /**
         * @return Has to be something so always null.
         */
        protected String defaultResult() {
            return null;
        }

        /**
         * @param ctx the retrieve.
         * @return Returns true if the retrieve is a valueset retrieve.
         */
        private boolean matchesValuesetPathRetrieve(cqlParser.RetrieveContext ctx) {
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
        private boolean matchesValuesetRetrieve(cqlParser.RetrieveContext ctx) {
            return ctx.getChildCount() == 7 &&
                    ctx.getChild(0).getText().equals("[") &&
                    ctx.getChild(2).getText().equals(":") &&
                    ctx.getChild(4).getText().equals("]");
        }


        /**
         * @param ctx The context
         * @return Returns true if the retrieve is a simple retrieve like "Test".
         */
        private boolean matchesSimpleRetrieve(cqlParser.RetrieveContext ctx) {
            return ctx.getChildCount() == 3;
        }

        /**
         * @param ctx The context.
         * @return Builds a DataRequirement from a simple retrieve.
         */
        private DataRequirement fromSimpleRetrieve(cqlParser.RetrieveContext ctx) {
            var result = new DataRequirement();
            result.setType(trim1(ctx.getChild(1).getText()));
            dataRequirements.add(result);
            return result;
        }

        /**
         * @param ctx The context.
         * @return Builds a DataRequirement from a valueset retrieve.
         */
        private DataRequirement fromValueSetPathRetrieve(cqlParser.RetrieveContext ctx) {
            var result = new DataRequirement();
            result.setType(trim1(ctx.getChild(1).getText()));
            var filter = new DataRequirement.DataRequirementCodeFilterComponent();
            filter.setPath(ctx.getChild(3).getText());
            String vsId = ctx.getChild(5).getText();
            filter.setValueSet(getValueSetUrl(vsId));
            result.setCodeFilter(Collections.singletonList(filter));
            return result;
        }

        /**
         * @param ctx The context.
         * @return Builds a DataRequirement from a valueset retrieve.
         */
        private DataRequirement fromValueSetRetrieve(cqlParser.RetrieveContext ctx) {
            var result = new DataRequirement();
            result.setType(trim1(ctx.getChild(1).getText()));
            var filter = new DataRequirement.DataRequirementCodeFilterComponent();
            String vsId = ctx.getChild(3).getText();
            filter.setValueSet(getValueSetUrl(vsId));
            result.setCodeFilter(Collections.singletonList(filter));
            return result;
        }

        /**
         * @param valueSetId The value set local id.
         * @return Returns true if the valueSetId is in this library.
         * False if it is an included library of this one.
         */
        private boolean isInIncludeLib(String valueSetId) {
            return !valueSetId.contains(".");
        }


        /**
         * Handles the valueset URL lookup for the specified valueSet id.
         * This is tricky because it might be an included lib.
         *
         * @param vsId The value set local id.
         * @return The value set url.
         */
        private String getValueSetUrl(String vsId) {
            if (isInIncludeLib(vsId)) {
                var optionalVs = valueSets.stream().filter(
                        vs -> StringUtils.equals(vs.identifier().getText(), vsId)).findFirst();
                if (optionalVs.isPresent()) {
                    return optionalVs.get().valuesetId().getText();
                } else {
                    throw new IllegalStateException("Could not find vs for local id " + vsId);
                }
            } else {
                String[] vsIdSplit = vsId.split("\\.");
                String libId = vsIdSplit[0];
                String splitVs = vsIdSplit[1];

                var foundInclude = includes.stream().filter(
                        i -> StringUtils.equals(defaultText(i.localIdentifier()), libId)).findFirst();
                if (foundInclude.isPresent()) {
                    var foundLib = libs.stream().filter(l -> matches(foundInclude.get(), l)).findFirst();
                    if (foundLib.isPresent()) {
                        return getValueSetUrl(splitVs, foundLib.get());
                    } else {
                        throw new IllegalStateException("Could not find value set for " + vsId);
                    }
                } else {
                    throw new IllegalStateException("Could not find lib for " + vsId);
                }
            }
        }

        private boolean matches(cqlParser.IncludeDefinitionContext ctx, Library lib) {
            var nameVersion = getNameVersionFromInclude(ctx);
            return StringUtils.equals(lib.getName(), nameVersion.getLeft()) &&
                    StringUtils.equals(lib.getVersion(), nameVersion.getRight());
        }

        /**
         * @param name localName
         * @param lib  Fhir Lib
         * @return Returns the valueset url of a valueset in a given library with the specified local name.
         */
        private String getValueSetUrl(String name, Library lib) {
            Optional<Attachment> a = lib.getContent().stream().filter(
                    c -> c.getContentType().equals("text/cql")).findFirst();
            if (a.isPresent()) {
                String cql = new String(Base64.getDecoder().decode(a.get().getData()), StandardCharsets.UTF_8);
                cqlParser.LibraryContext library = cqlAntlrUtils.getLibraryContext(cql);
                var optionalVs = library.valuesetDefinition().stream().filter(
                        vs -> StringUtils.equals(vs.identifier().getText(), name)).findFirst();
                if (optionalVs.isPresent()) {
                    return optionalVs.get().valuesetId().getText();
                } else {
                    throw new IllegalStateException("Could not find value set " + name +
                            " in lib " + lib.getName() + "|" + lib.getVersion());
                }
            } else {
                throw new IllegalStateException("Could not find text/cql for lib: " + lib.getId());
            }
        }

        private String trim1(String s) {
            if (StringUtils.isNotBlank(s) && s.length() > 2) {
                return s.substring(1, s.length() - 1);
            } else {
                return s;
            }
        }

        private String defaultText(ParserRuleContext c) {
            return c == null ? null : c.getText();
        }
    }
}
