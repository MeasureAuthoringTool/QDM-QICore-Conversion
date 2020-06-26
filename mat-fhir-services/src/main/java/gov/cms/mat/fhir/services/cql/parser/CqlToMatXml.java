package gov.cms.mat.fhir.services.cql.parser;

import gov.cms.mat.fhir.services.components.vsac.VsacRestClient;
import gov.cms.mat.fhir.services.repository.CqlLibraryRepository;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import mat.model.cql.CQLCode;
import mat.model.cql.CQLCodeSystem;
import mat.model.cql.CQLDefinition;
import mat.model.cql.CQLFunctionArgument;
import mat.model.cql.CQLFunctions;
import mat.model.cql.CQLIncludeLibrary;
import mat.model.cql.CQLModel;
import mat.model.cql.CQLParameter;
import mat.model.cql.CQLQualityDataSetDTO;
import mat.model.cql.VsacStatus;
import mat.shared.CQLError;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static gov.cms.mat.fhir.services.cql.parser.CqlUtils.chomp1;
import static gov.cms.mat.fhir.services.cql.parser.CqlUtils.isQuoted;
import static gov.cms.mat.fhir.services.cql.parser.CqlUtils.newGuid;
import static gov.cms.mat.fhir.services.cql.parser.CqlUtils.parseCodeSystemName;
import static gov.cms.mat.fhir.services.cql.parser.CqlUtils.parseOid;
import static gov.cms.mat.fhir.services.cql.parser.CqlUtils.validateValuesetUri;
import static gov.cms.mat.fhir.services.cql.parser.CqlUtils.versionToVersionAndRevision;

/**
 * A CqlVisitor for converting FHIR to MatXml format without a source model.
 * This version determines the correct ValueSets, Codes, and CodeSystems from the MAT DB.
 */
@Getter
@Setter
@ToString
@Slf4j
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CqlToMatXml implements CqlVisitor {
    /**
     * %1s is the code system oid.
     * %2s is the code system version.
     * %3s os the code value.
     */
    public static final String CODE_IDENTIFIER_FORMAT = "CODE:/CodeSystem/%1$s/Version/%2$s/Code/%3$s/Info";
    public static final String LIB_NAME_REGEX = "^[A-Z]([A-Za-z0-9_]){0,254}$";
    public static final String LIB_VERSION_REGEX = "^(([1-9][0-9][0-9])|([1-9][0-9])|([0-9])).(([1-9][0-9][0-9])|([1-9][0-9])|([0-9])).[0-9][0-9][0-9]$";
    public static final Pattern LIBRARY_NAME_PATTERN = Pattern.compile(LIB_NAME_REGEX);
    public static final Pattern LIBRARY_VERSION_PATTERN = Pattern.compile(LIB_VERSION_REGEX);

    @Value("${mat.codesystem.valueset.simultaneous.validations}")
    private int simultaneousValidations;
    @Value("${mat.qdm.default.expansion.id}")
    private String defaultExpId;
    private List<CQLIncludeLibrary> libsNotFound = new ArrayList<>();
    private List<CQLError> severes = new ArrayList<>();
    private List<CQLError> errors = new ArrayList<>();
    private List<CQLError> warnings = new ArrayList<>();
    private CQLModel destinationModel = new CQLModel();
    private CQLModel sourceModel;
    private boolean isValidatingCodesystems = true;
    private boolean isValidatingValuesets = true;
    private ExecutorService codeSystemValueSetExecutor;
    private String umlsToken;

    private final CqlLibraryRepository cqlLibraryRepository;
    private final CodeListService codeListService;
    private final VsacRestClient vsacRestClient;

    public CqlToMatXml(CodeListService codeListService, CqlLibraryRepository cqlLibraryRepository, VsacRestClient vsacRestClient) {
        this.codeListService = codeListService;
        this.cqlLibraryRepository = cqlLibraryRepository;
        this.vsacRestClient = vsacRestClient;
    }

    @PostConstruct
    protected void setupExecutor() {
        codeSystemValueSetExecutor = Executors.newFixedThreadPool(simultaneousValidations);

        // Appease the codacy gods. Want to keep this code for future use.
        // Remove this if it is being used.
        errorAtLine("foo",1);
    }

    @Override
    public void handleError(CQLError e) {
        if (StringUtils.equalsIgnoreCase("Warning", e.getSeverity())) {
            warnings.add(e);
        } else if (StringUtils.equalsIgnoreCase("Error", e.getSeverity())) {
            errors.add(e);
        } else {
            severes.add(e);
        }
    }

    @Override
    public void validateBeforeParse() {
        if (sourceModel == null) {
            // If source model is null just create a new empty CQLModel.
            sourceModel = new CQLModel();
        }
    }

    @Override
    public void libraryTag(String libraryName, String version, @Nullable String libraryComment, int lineNumber) {
        if (!LIBRARY_NAME_PATTERN.matcher(libraryName).matches()) {
            handleError(severErrorAtLine("Library name must follow this regex: " + LIB_NAME_REGEX +
                    ". You are only allowed to change this in General Information. It is called CQL Library name.", lineNumber));
        }
        if (!LIBRARY_VERSION_PATTERN.matcher(version).matches()) {
            handleError(severErrorAtLine("Library version must follow this regex: " + LIB_VERSION_REGEX + ", e.g. 1.0.000", lineNumber));
        }

        destinationModel.setLibraryName(libraryName);
        destinationModel.setVersionUsed(version);
        destinationModel.setLibraryComment(libraryComment);
    }

    @Override
    public void usingModelVersionTag(String model, String modelVersion) {
        destinationModel.setUsingModelVersion(modelVersion);
        destinationModel.setUsingModel(model);
    }

    @Override
    public void includeLib(String libName, String version, String alias, String model, String modelVersion, int lineNumber) {
        if (!LIBRARY_NAME_PATTERN.matcher(libName).matches()) {
            handleError(severErrorAtLine("Library name must follow this regex: " + LIB_NAME_REGEX, lineNumber));
        }
        if (!LIBRARY_VERSION_PATTERN.matcher(version).matches()) {
            handleError(severErrorAtLine("Library version must follow this regex: " + LIB_VERSION_REGEX + ", e.g. 1.0.000", lineNumber));
        }

        CQLIncludeLibrary lib = new CQLIncludeLibrary();
        lib.setId(newGuid());
        lib.setCqlLibraryName(libName);
        lib.setVersion(version);
        lib.setAliasName(alias);
        lib.setLibraryModelType(model);
        lib.setQdmVersion(modelVersion);
        var versionPair = versionToVersionAndRevision(version);
        var cqlLibrary = cqlLibraryRepository.findByVersionAndCqlNameAndRevisionNumberAndLibraryModelAndDraft(new BigDecimal(versionPair.getLeft()),
                libName,
                versionPair.getRight(),
                model, false);
        if(cqlLibrary != null) {
            lib.setCqlLibraryId(cqlLibrary.getId());
        } else {
            handleError(severErrorAtLine("Could not find library " + libName + " " + version, lineNumber));
        }
        destinationModel.getCqlIncludeLibrarys().add(lib);
    }

    @Override
    public void codeSystem(String name, String uri, String versionUri, int lineNumber) {
        var parsedCodeSystemName = parseCodeSystemName(name);
        CQLCodeSystem cs = new CQLCodeSystem();
        cs.setId(newGuid());
        cs.setCodeSystemName(name);
        cs.setCodeSystemVersion(StringUtils.defaultString(parsedCodeSystemName.getRight()));
        cs.setCodeSystem(uri);
        cs.setVersionUri(versionUri);
        cs.setLineNumber(lineNumber);
        destinationModel.getCodeSystemList().add(cs);
    }

    @Override
    public void valueSet(String name, String uri, int lineNumber) {
        try {
            validateValuesetUri(uri);
        } catch (IllegalArgumentException e) {
            handleError(severErrorAtLine("Invalid valueset URI: " + uri, lineNumber));
        }

        var vs = new CQLQualityDataSetDTO();
        vs.setId(newGuid());
        vs.setName(name);
        vs.setUuid(newGuid());
        vs.setOid(uri);

        // Nonsensical legacy stuff that has to be here for the gwt part to function at the moment.
        vs.setOriginalCodeListName(name);
        vs.setSuppDataElement(false);
        vs.setTaxonomy("Grouping");
        vs.setType("Grouping");

        var existingValueSet = findExisting(sourceModel.getValueSetList(),
                evs -> StringUtils.equals(parseOid(evs.getOid()), parseOid(vs.getOid())));
        existingValueSet.ifPresentOrElse(v -> vs.setValidatedWithVsac(v.isValidatedWithVsac()),
                () -> vs.setValidatedWithVsac(VsacStatus.PENDING.name()));

        destinationModel.getValueSetList().add(vs);
    }

    @Override
    public void code(String name, String code, String codeSystemName, String displayName, int lineNumber) {
        var c = new CQLCode();
        c.setCodeSystemName(codeSystemName);
        c.setCodeOID(code);
        c.setDisplayName(StringUtils.isEmpty(displayName) ? name : displayName);
        c.setCodeName(name);
        c.setId(newGuid());
        c.setLineNumber(lineNumber);

        updateCodeSystemInfo(c);
        updateCodeIdentifierAndVsacValidationFlag(c, lineNumber);

        destinationModel.getCodeList().add(c);
    }

    @Override
    public void parameter(String name, String logic, String comment) {
        var existingParam = findExisting(sourceModel.getCqlParameters(),
                p -> StringUtils.equals(p.getName(), name));

        CQLParameter p = new CQLParameter();
        p.setId(newGuid());
        p.setName(name);
        p.setParameterLogic(logic);
        p.setCommentString(comment);
        if (existingParam.isPresent()) {
            p.setReadOnly(existingParam.get().isReadOnly());
        } else {
            p.setReadOnly(false);
        }

        destinationModel.getCqlParameters().add(p);
    }

    @Override
    public void context(String context) {
        destinationModel.setContext(context);
    }

    @Override
    public void definition(String name, String logic, String comment) {
        destinationModel.getDefinitionList().add(buildCQLDef(name, logic, comment));
    }

    @Override
    public void function(String name, List<FunctionArgument> args, String logic, String comment) {
        // Functions can be overloaded in FHIR
        var f = new CQLFunctions();
        f.setId(newGuid());
        f.setName(name);
        f.setLogic(logic);
        f.setCommentString(comment);
        f.setArgumentList(args.stream().map(a -> {
            CQLFunctionArgument argument = new CQLFunctionArgument();
            argument.setId(newGuid());
            argument.setArgumentName(a.getName());
            if (isQuoted(a.getType())) {
                //In conversions we will still have quoted strings here for QDM type.
                argument.setQdmDataType(chomp1(a.getType()));
                argument.setArgumentType("QDM Datatype");
            } else {
                argument.setQdmDataType(a.getType());
                argument.setArgumentType("FHIR Datatype");
            }
            return argument;
        }).collect(Collectors.toList()));
        f.setContext(destinationModel.getContext());
        destinationModel.getCqlFunctions().add(f);
    }

    /**
     * @param title   The title.
     * @param logic   The logic.
     * @param comment The comment.
     * @return The CQLDefinition with all the defaults and params populated.
     */
    private CQLDefinition buildCQLDef(String title, String logic, String comment) {
        CQLDefinition result = new CQLDefinition();
        result.setId(newGuid());
        result.setName(title);
        result.setLogic(logic);
        result.setSupplDataElement(false);
        result.setPopDefinition(false);
        result.setCommentString(comment);
        return result;
    }

    private <T> Optional<T> findExisting(List<T> collection, Predicate<T> filter) {
        return collection.stream().filter(filter).findFirst();
    }

    private void updateCodeSystemInfo(CQLCode c) {
        var currentCS = destinationModel.getCodeSystemList().stream().filter(cs ->
                StringUtils.equals(cs.getCodeSystemName(), c.getCodeSystemName())).findFirst();

        var previousCode = findExisting(sourceModel.getCodeList(),
                ec -> StringUtils.equals(ec.getCodeName(), c.getCodeName()) &&
                        StringUtils.equals(ec.getCodeSystemName(), c.getCodeSystemName()));

        if (currentCS.isPresent()) {

            c.setCodeSystemName(currentCS.get().getCodeSystemName());
            c.setIsCodeSystemVersionIncluded(c.getCodeSystemName().contains(":"));
            c.setCodeSystemOID(currentCS.get().getCodeSystem());

            if (c.isIsCodeSystemVersionIncluded()) {
                c.setCodeSystemVersion(parseCodeSystemName(c.getCodeSystemName()).getRight());
            } else {
                previousCode.ifPresent(cqlCode -> c.setCodeSystemVersion(cqlCode.getCodeSystemVersion()));
            }

            if (StringUtils.isEmpty(c.getCodeSystemVersion()) && StringUtils.isNotEmpty(umlsToken)) {
                VsacRestClient.CodeSystemVersionResponse response = vsacRestClient.fetchVersionFromName(currentCS.get().getCodeSystemName(), umlsToken);

                if (response.getSuccess()) {
                    c.setCodeSystemVersion(response.getVersion());
                } else {
                    handleError(errorAtLine(response.getMessage(), c.getLineNumber()));
                }
            }

            c.setCodeSystemVersionUri(currentCS.get().getVersionUri());


//            if (StringUtils.isBlank(c.getCodeSystemVersion())) {
//                //Grab it from vsacCodeSysteMap spread sheet.
//                var map = codeListService.getOidToVsacCodeSystemMap().values();
//                var vsacCodeSystem = map.stream().filter(
//                        v -> StringUtils.equals(v.getUrl(), c.getCodeSystemOID())).findFirst();
//
//                if (vsacCodeSystem.isEmpty()) {
//                    //Hard error since we can't find the an entry in the spreadsheet.
//                    handleError(severErrorAtLine("This code system uri is not permitted. " +
//                                    "Please contact the MAT team if you feel this is in error.",
//                            currentCS.get().getLineNumber()));
//                } else {
//                    c.setCodeSystemVersion(vsacCodeSystem.get().getDefaultVsacVersion());
//                }
//            }
        } else {
            //Hard error since we can't find the existing code system in the cql.
            handleError(severErrorAtLine("Could not find code system named " +
                            c.getCodeSystemName() + ".",
                    c.getLineNumber()));
        }
    }

    private void updateCodeIdentifierAndVsacValidationFlag(CQLCode incoming, int lineNumber) {
        var existingCodeOptional = findExisting(sourceModel.getCodeList(),
                ec -> StringUtils.equals(ec.getCodeName(), incoming.getCodeName()) &&
                        StringUtils.equals(ec.getCodeSystemName(), incoming.getCodeSystemName()));


        incoming.addValidatedWithVsac(existingCodeOptional.map(CQLCode::obtainValidatedWithVsac).orElse(VsacStatus.PENDING));

        if (incoming.obtainValidatedWithVsac() == VsacStatus.VALID) {
            var existingCodeSystem = findExisting(sourceModel.getCodeSystemList(),
                    ecs -> StringUtils.equals(ecs.getCodeSystem(), incoming.getCodeSystemOID()));
            if (existingCodeSystem.isEmpty()) {
                incoming.addValidatedWithVsac(existingCodeOptional.map(CQLCode::obtainValidatedWithVsac).
                        orElse(VsacStatus.PENDING));
            }
        }

        String codeIdentifier = buildCodeIdentifier(incoming, lineNumber);

        // if existing not found force validation
        if (codeIdentifier != null) {
            incoming.setCodeIdentifier(codeIdentifier);

            if (incoming.obtainValidatedWithVsac() == VsacStatus.VALID && existingCodeOptional.isPresent()) {
                CQLCode existingCode = existingCodeOptional.get();
                String existingCodeIdentifier = buildCodeIdentifier(existingCode, lineNumber);

                if (existingCodeIdentifier == null || !existingCodeIdentifier.equals(codeIdentifier)) {
                    incoming.addValidatedWithVsac(VsacStatus.PENDING);
                }
            }
        }
    }

    /**
     * Builds the code identifier vsac url.
     *
     * @param c The CQLCode to build the Identifier for.
     * @return The VSAC code url, e.g. .
     */
    private String buildCodeIdentifier(CQLCode c, int lineNumber) {

        if (StringUtils.isEmpty(c.getCodeSystemVersion())) {
            return null;
        } else {
            return String.format(CODE_IDENTIFIER_FORMAT,
                    parseCodeSystemName(c.getCodeSystemName()).getLeft(),
                    StringUtils.isBlank(c.getCodeSystemVersionUri()) ? c.getCodeSystemVersion() : processCodeSystemVersionUri(c.getCodeSystemVersionUri()),
                    c.getCodeOID());
        }

//        var vsacCodeSystem = codeListService.getOidToVsacCodeSystemMap().values().stream().filter(
//                v -> StringUtils.equals(v.getUrl(), c.getCodeSystemOID())).findFirst();
//
//        if (vsacCodeSystem.isPresent()) {
//            String defaultVersion = vsacCodeSystem.get().getDefaultVsacVersion();
//
//            //todo use vsac to get latest version MCG add story
//            return String.format(CODE_IDENTIFIER_FORMAT,
//                    parseCodeSystemName(c.getCodeSystemName()).getLeft(),
//                    StringUtils.isBlank(c.getCodeSystemVersionUri()) ? defaultVersion : processCodeSystemVersionUri(c.getCodeSystemVersionUri()),
//                    c.getCodeOID());
//        } else {
//
//            return null;
//        }
    }


    private String processCodeSystemVersionUri(String codeSystemVersionUriIn) {

        String codeSystemVersionUri = codeSystemVersionUriIn;

        if (codeSystemVersionUri.startsWith("http://snomed.info/")) {
            String version = StringUtils.substringAfter(codeSystemVersionUri, "/version/");

            if (StringUtils.isEmpty(version)) {
                log.warn("Cannot find SNOMED version in codeSystemVersionUri: {}", codeSystemVersionUri);
            } else if (version.length() != 6) { //201907 YYYYMM
                log.warn("Version string length is not 6: {}", version);
            } else {
                codeSystemVersionUri = version.substring(0, 4) + "-" + version.substring(4);
            }
        }

        return codeSystemVersionUri;
    }

    private CQLError severErrorAtLine(String msg, int lineNumber) {
        CQLError e = new CQLError();
        e.setSeverity("Severe");
        e.setErrorMessage(msg);
        e.setErrorInLine(lineNumber);
        e.setErrorAtOffset(0);
        e.setStartErrorInLine(lineNumber);
        e.setEndErrorInLine(lineNumber);
        return e;
    }

    private CQLError errorAtLine(String msg, int lineNumber) {
        CQLError e = new CQLError();
        e.setSeverity("Error");
        e.setErrorMessage(msg);
        e.setErrorInLine(lineNumber);
        e.setErrorAtOffset(0);
        e.setStartErrorInLine(lineNumber);
        e.setEndErrorInLine(lineNumber);
        return e;
    }
}
