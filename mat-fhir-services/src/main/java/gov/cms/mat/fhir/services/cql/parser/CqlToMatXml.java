package gov.cms.mat.fhir.services.cql.parser;

import gov.cms.mat.fhir.services.components.validation.CodeSystemVsacAsync;
import gov.cms.mat.fhir.services.repository.CqlLibraryRepository;
import gov.cms.mat.fhir.services.summary.CodeSystemEntry;
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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
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
    public static final String SNOWMED_URL = "http://snomed.info/sct";

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
    private String umlsToken;

    private final CqlLibraryRepository cqlLibraryRepository;
    private final CodeListService codeListService;

    public CqlToMatXml(CodeListService codeListService, CqlLibraryRepository cqlLibraryRepository) {
        this.codeListService = codeListService;
        this.cqlLibraryRepository = cqlLibraryRepository;
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
        } else if (!LIBRARY_VERSION_PATTERN.matcher(version).matches()) {
            handleError(severErrorAtLine("Library version must follow this regex: " + LIB_VERSION_REGEX + ", e.g. 1.0.000", lineNumber));
        } else {
            CQLIncludeLibrary lib = new CQLIncludeLibrary();
            lib.setId(newGuid());
            lib.setCqlLibraryName(libName);
            lib.setVersion(version);
            lib.setAliasName(alias);
            lib.setLibraryModelType(model);
            lib.setQdmVersion(modelVersion);
            var versionPair = versionToVersionAndRevision(version);
            var cqlLibrary = cqlLibraryRepository.findByVersionAndCqlNameAndRevisionNumberAndLibraryModelAndDraft(
                    BigDecimal.valueOf(versionPair.getLeft()),
                    libName,
                    versionPair.getRight(),
                    model, false);
            if (cqlLibrary != null) {
                lib.setCqlLibraryId(cqlLibrary.getId());
            } else {
                handleError(severErrorAtLine("Could not find library " + libName + " " + version, lineNumber));
            }
            destinationModel.getCqlIncludeLibrarys().add(lib);
        }
    }

    @Override
    public void codeSystem(String name, String uri, String versionUri, int lineNumber) {
        var parsedCodeSystemName = parseCodeSystemName(name);
        var isConversion = !sourceModel.isFhir();
        CQLCodeSystem cs = new CQLCodeSystem();
        cs.setId(newGuid());
        cs.setVersionUri(versionUri);
        cs.setCodeSystemName(parsedCodeSystemName.getLeft());
        cs.setCodeSystemVersion(parsedCodeSystemName.getRight());
        cs.setCodeSystem(uri);

        if (isConversion) {
            updateCodeSystemForConversion(cs);
        }
        updateCodeSystemVersionFromVersionUri(cs);

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
        var pair = parseCodeSystemName(codeSystemName);
        c.setCodeSystemName(pair.getLeft());
        c.setCodeSystemVersion(pair.getRight());
        c.setCodeOID(code);
        c.setDisplayName(StringUtils.isEmpty(displayName) ? name : displayName);
        c.setCodeName(name);
        c.setId(newGuid());
        c.setLineNumber(lineNumber);
        updateCodeSystemFields(c);
        updateVsacValidation(c);
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
        f.setId(getFunctionId(name));
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

    private String getDefineId(String name) {
        // Look for an existing one and use that if found. Otherwise generate a new one.
        if (sourceModel != null) {
            var matchingDefines = sourceModel.getDefinitionList().stream().
                    filter(d -> StringUtils.equals(d.getName(),name)).
                    collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(matchingDefines) && matchingDefines.size() == 1) {
                return matchingDefines.get(0).getId();
            } else {
                return newGuid();
            }
        } else {
            return newGuid();
        }
    }

    private String getFunctionId(String name) {
        // Look for an existing one and use that if found. Otherwise generate a new one.
        if (sourceModel != null) {
            var matchingFunctions = sourceModel.getCqlFunctions().stream().
                    filter(d -> StringUtils.equals(d.getName(),name)).
                    collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(matchingFunctions) && matchingFunctions.size() == 1) {
                return matchingFunctions.get(0).getId();
            } else {
                return newGuid();
            }
        } else {
            return newGuid();
        }
    }

    /**
     * @param title   The title.
     * @param logic   The logic.
     * @param comment The comment.
     * @return The CQLDefinition with all the defaults and params populated.
     */
    private CQLDefinition buildCQLDef(String title, String logic, String comment) {
        CQLDefinition result = new CQLDefinition();
        result.setId(getDefineId((title)));
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

    private void updateCodeSystemFields(CQLCode c) {
        var currentCS = getCodeSystemForCode(c);

        if (currentCS.isPresent()) {
            c.setCodeSystemName(currentCS.get().getCodeSystemName());
            c.setCodeSystemVersion(currentCS.get().getCodeSystemVersion());
            c.setIsCodeSystemVersionIncluded(StringUtils.isNotBlank(currentCS.get().getVersionUri()));
            c.setCodeSystemVersionUri(currentCS.get().getVersionUri());
            c.setCodeSystemOID(currentCS.get().getCodeSystem());

            if (!isInSpreadsheet(c)) {
                handleError(severErrorAtLine("This code system uri is not permitted. " +
                                "Please contact the MAT team if you feel this is in error.",
                        currentCS.get().getLineNumber()));
            }
        } else {
            //Hard error since we can't find the existing code system in the cql.
            handleError(severErrorAtLine("Could not find code system named " +
                            c.getCodeSystemName() + ".",
                    c.getLineNumber()));
        }
    }

    private void updateVsacValidation(CQLCode c) {
        // This method also updates code identifier (otherwise known as the VSAC code lookup url).
        var currentCS = getCodeSystemForCode(c);
        var previousCode = getPreviousCode(c);
        var isConversion = !sourceModel.isFhir();
        boolean isSpreadSheetRowInVsac = isCodeInVsac(c);

        if (isConversion) {
            // If we found the previous code it is valid and CodeIdentifier is previous.
            // If not found PENDING and code identifier is null.
            previousCode.ifPresentOrElse(pc -> {
                c.addValidatedWithVsac(VsacStatus.VALID);
                c.setCodeIdentifier(pc.getCodeIdentifier());
            }, () -> updateToRequireVsacValidation(c));
        } else if (previousCode.isPresent()) {
            if (!isSpreadSheetRowInVsac) {
                //Always valid.
                c.addValidatedWithVsac(VsacStatus.VALID);
                c.setCodeIdentifier(previousCode.get().getCodeIdentifier());
            } else {
                // If code system changed or code changed then its pending.
                if (hasCodeOrCodeSystemChanged(currentCS, c, previousCode)) {
                    updateToRequireVsacValidation(c);
                } else {
                    // If nothing changed it is the status of previous code.
                    c.setValidatedWithVsac(previousCode.get().getValidatedWithVsac());
                    c.setCodeIdentifier(previousCode.get().getCodeIdentifier());
                }
            }
        } else {
            // A new code was added.
            // If is in vsac needs validation, otherwise it is valid.
            if (!isSpreadSheetRowInVsac) {
                updateToRequireVsacValidation(c);
            } else {
                c.addValidatedWithVsac(VsacStatus.VALID);
                c.setCodeIdentifier("");
            }
        }
    }

    private boolean hasCodeOrCodeSystemChanged(Optional<CQLCodeSystem> cs,
                                               CQLCode currentCode,
                                               Optional<CQLCode> previousCode) {
        return cs.isEmpty() || previousCode.isEmpty() ||
                !StringUtils.equals(currentCode.getCodeName(), previousCode.get().getCodeName()) ||
                !StringUtils.equals(currentCode.getCodeOID(), previousCode.get().getCodeOID()) ||
                !StringUtils.equals(currentCode.getCodeSystemVersionUri(), previousCode.get().getCodeSystemVersionUri()) ||
                !StringUtils.equals(currentCode.getCodeSystemOID(), previousCode.get().getCodeSystemOID());
    }

    private boolean isCodeInVsac(CQLCode c) {
        // If not in spreadsheet just return false.
        AtomicBoolean result = new AtomicBoolean(false);
        getSpreadsheetRow(c).ifPresent(cse ->
                result.set(!StringUtils.contains(cse.getOid(), CodeSystemVsacAsync.NOT_IN_VSAC)));
        return result.get();
    }

    private boolean isInSpreadsheet(CQLCode c) {
        return getSpreadsheetRow(c).isPresent();
    }

    private Optional<CodeSystemEntry> getSpreadsheetRow(CQLCode c) {
        return codeListService.getOidToVsacCodeSystemMap().values().stream().filter(
                v -> StringUtils.equals(v.getUrl(), c.getCodeSystemOID())).findFirst();
    }

    private Optional<CQLCode> getPreviousCode(CQLCode c) {
        return findExisting(sourceModel.getCodeList(),
                ec -> StringUtils.equals(ec.getCodeName(), c.getCodeName()) &&
                        StringUtils.equals(ec.getCodeSystemName(), c.getCodeSystemName()));
    }

    private Optional<CQLCodeSystem> getCodeSystemForCode(CQLCode c) {
        // Lookup by code system name.
        return destinationModel.getCodeSystemList().stream().filter(cs ->
                StringUtils.equals(cs.getCodeSystemName(), c.getCodeSystemName())).findFirst();
    }

    private void updateToRequireVsacValidation(CQLCode c) {
        c.addValidatedWithVsac(VsacStatus.PENDING);
        c.setCodeIdentifier("");
    }

    private CQLError severErrorAtLine(String msg, int lineNumber) {
        return createError(msg, "Severe", lineNumber);
    }

    private CQLError errorAtLine(String msg, int lineNumber) {
        return createError(msg, "Error", lineNumber);
    }

    private CQLError warnAtLine(String msg, int lineNumber) {
        return createError(msg, "Warn", lineNumber);
    }

    private CQLError createError(String msg, String sevrity, int lineNumber) {
        CQLError e = new CQLError();
        e.setSeverity(sevrity);
        e.setErrorMessage(msg);
        e.setErrorInLine(lineNumber);
        e.setErrorAtOffset(0);
        e.setStartErrorInLine(lineNumber);
        e.setEndErrorInLine(lineNumber);
        return e;
    }

    public static void updateCodeSystemForConversion(CQLCodeSystem cs) {
        // On conversion urn:hl7:version: is still left in the version URI.
        // This code fixes that.
        if (StringUtils.isNotBlank(cs.getVersionUri())) {
            String cleanedVersionUri = StringUtils.remove(cs.getVersionUri(), "urn:hl7:version:");
            if (StringUtils.equals(SNOWMED_URL, cs.getCodeSystem())) {
                String cleanedVersion = StringUtils.remove(cleanedVersionUri, '-');
                cs.setVersionUri(SNOWMED_URL + "/version/" + cleanedVersion);
            } else {
                cs.setVersionUri(cleanedVersionUri);
            }
        }
    }

    public static void updateCodeSystemVersionFromVersionUri(CQLCodeSystem cs) {
        // CQLCodeSystem version is always driven off of versionUri.
        String versionUri = cs.getVersionUri();
        if (StringUtils.isNotBlank(versionUri)) {
            if (cs.getCodeSystem().equals(SNOWMED_URL)) {
                String version = StringUtils.substringAfter(versionUri, "/version/");
                if (StringUtils.isEmpty(version)) {
                    log.warn("Cannot find SNOMED version in codeSystemVersionUri: {}", versionUri);
                } else if (version.length() != 6) { //201907 YYYYMM
                    log.warn("Version string length is not 6: {}", version);
                } else {
                    cs.setCodeSystemVersion(version.substring(0, 4) + "-" + version.substring(4));
                }
            } else {
                cs.setCodeSystemVersion(versionUri);
            }
        } else {
            cs.setCodeSystemVersion(null);
        }
    }
}
