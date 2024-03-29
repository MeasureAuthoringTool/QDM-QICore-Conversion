package gov.cms.mat.fhir.services.cql;

import gov.cms.mat.cql.CqlTextParser;
import gov.cms.mat.cql.elements.BaseProperties;
import gov.cms.mat.cql.elements.CodeSystemProperties;
import gov.cms.mat.cql.elements.DefineProperties;
import gov.cms.mat.cql.elements.IncludeProperties;
import gov.cms.mat.cql.elements.SymbolicProperty;
import gov.cms.mat.cql.elements.UnionProperties;
import gov.cms.mat.cql.elements.UsingProperties;
import gov.cms.mat.cql.elements.ValueSetProperties;
import gov.cms.mat.fhir.rest.dto.spreadsheet.CodeSystemEntry;
import gov.cms.mat.fhir.services.components.reporting.ConversionReporter;
import gov.cms.mat.fhir.services.exceptions.CodeSystemOidNotFoundException;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.service.MappingDataService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.Resource;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static gov.cms.mat.cql.elements.DefineProperties.DEFINE_SDE;

@Slf4j
public class QdmCqlToFhirCqlConverter {
    private static final String SDE_TEMPLATE = "  SDE.\"SDE %s\"";

    private static final StandardLib[] STANDARD_LIBS = {
            new StandardLib("FHIRHelpers", "4.1.000", "FHIRHelpers"),
            new StandardLib("SupplementalDataElementsFHIR4", "3.0.000", "SDE"),
            new StandardLib("MATGlobalCommonFunctionsFHIR4", "7.0.000", "Global")
    };

    private static final String ERROR_MESSAGE =
            "DEFINE crosses dissimilar FHIR Resources within UNION statements, this will fail processing, " +
                    "consider creating define statements limited to single FHIR Resource.";
    private final CqlTextParser cqlTextParser;
    private final MappingDataService mappingDataService;
    private final Map<String, String> conversionLibLookupMap;
    private final List<CodeSystemEntry> codeSystemMappings;
    private final HapiFhirServer hapiFhirServer;

    private final boolean includeStdLibraries;

    List<IncludeProperties> includeProperties;
    List<IncludeProperties> standardIncludeProperties;
    private UsingProperties usingProperties;

    public QdmCqlToFhirCqlConverter(String cqlText,
                                    boolean includeStdLibraries,
                                    MappingDataService mappingDataService,
                                    Map<String, String> conversionLibLookupMap,
                                    List<CodeSystemEntry> codeSystemMappings,
                                    HapiFhirServer hapiFhirServer) {
        cqlTextParser = new CqlTextParser(cqlText);
        this.mappingDataService = mappingDataService;
        this.conversionLibLookupMap = conversionLibLookupMap;
        this.codeSystemMappings = codeSystemMappings;
        this.hapiFhirServer = hapiFhirServer;
        standardIncludeProperties = createStandardIncludes();

        this.includeStdLibraries = includeStdLibraries;
    }

    private List<IncludeProperties> createStandardIncludes() {
        return Arrays.stream(STANDARD_LIBS)
                .map(this::createStandardInclude)
                .collect(Collectors.toList());
    }

    private String createStandardIncludesCql() {
        StringBuilder stringBuilder = new StringBuilder("\n");

        standardIncludeProperties.forEach(s -> stringBuilder.append(s.createCql()).append("\n"));

        return stringBuilder.toString();
    }

    private IncludeProperties createStandardInclude(StandardLib standardLib) {
        return IncludeProperties.builder()
                .name(standardLib.name)
                .version(standardLib.version)
                .called(standardLib.defaultCalled)
                .build();
    }

    public String convert(String matLibId) {
        includeProperties = cqlTextParser.getIncludes();

        convertLibrary();
        convertUsing();
        convertIncludes();
        convertValueSets();
        convertCodeSystems();
        checkUnion(matLibId);

        String cql = addDefaultFhirLibraries();

        cql = fixDateTime(cql);
        return fixSDE(cql);
    }

    private String fixDateTime(String cql) {
        return cql.replace("Patient.birthDatetime", "Patient.birthDate");
    }

    private void convertValueSets() {
        List<ValueSetProperties> properties = cqlTextParser.getValueSets();
        properties.forEach(this::setToFhir);
    }

    private void convertCodeSystems() {
        List<CodeSystemProperties> properties = cqlTextParser.getCodeSystems();

        properties.forEach(this::processCodeSystem);

        properties.forEach(this::setToFhir);
    }

    private void processCodeSystem(CodeSystemProperties codeSystemProperties) {
        processCodeSystemInGlobalMap(codeSystemProperties);
    }

    private void processCodeSystemInGlobalMap(CodeSystemProperties codeSystemProperties) {
        CodeSystemEntry entry = findCodeSystemEntry(codeSystemProperties.getUrnOid());
        codeSystemProperties.setUrnOid(entry.getUrl());
    }

    private CodeSystemEntry findCodeSystemEntry(String urnOid) {
        return codeSystemMappings
                .stream()
                .filter(e -> StringUtils.equals(urnOid, e.getOid()))
                .findFirst()
                .orElseThrow(() -> new CodeSystemOidNotFoundException(urnOid));
    }

    private String fixSDE(String cql) {
        String[] lines = cql.split("\\r?\\n");
        StringBuilder output = new StringBuilder();

        boolean isLastLineSDE = false;
        String lastLine = null;

        for (String line : lines) {
            if (isLastLineSDE) {
                String changedCql = createSdeCql(lastLine);
                output.append(changedCql);
            } else {
                output.append(line);
            }
            output.append("\n");

            isLastLineSDE = line.startsWith(DEFINE_SDE);
            lastLine = line;
        }

        return output.toString();
    }

    private String createSdeCql(String line) {
        String type = StringUtils.substringBetween(line, "\"SDE ", "\":");

        if (StringUtils.isEmpty(type)) {
            log.warn("Cannot successfully parse SDE line: {}", line);
            return line;
        } else {
            return String.format(SDE_TEMPLATE, type);
        }
    }

    private String addDefaultFhirLibraries() {
        String cqlUsingLine = usingProperties.createCql();

        if (includeStdLibraries) {
            String cqlReplacement = cqlUsingLine + createStandardIncludesCql();

            String cql = cqlTextParser.getCql()
                    .replace(cqlUsingLine, cqlReplacement);
            return cleanLines(cql);
        } else {
            return cleanLines(cqlTextParser.getCql());
        }
    }

    private String cleanLines(String cql) {
        String[] lines = cql.split("\\r?\\n");
        StringBuilder output = new StringBuilder();

        boolean isLastLineBlank = false;

        for (String line : lines) {
            boolean isLineBlank = StringUtils.isBlank(line);

            if (isLastLineBlank && isLineBlank) {
                log.debug("Skipping line");
            } else {
                output.append(line);
                output.append("\n");
            }

            isLastLineBlank = isLineBlank;
        }

        return output.toString();
    }

    private void checkUnion(String matLibId) {
        List<UnionProperties> unions = cqlTextParser.getUnions();
        var optional = unions.stream().filter(u -> !checkUnion(u)).findFirst();

        if (optional.isPresent()) {
            log.debug("We have library union issue");

            if (matLibId != null) {
                ConversionReporter.setCqlConversionErrorMessage(ERROR_MESSAGE, matLibId);
            }
        }
    }

    private boolean checkUnion(UnionProperties unionProperties) {
        List<SymbolicProperty> symbolicProperties = cqlTextParser.getSymbolicProperties(unionProperties.getLines());

        var optionalSymbolic = symbolicProperties.stream().filter(s -> s.getSymbolic() != null).findFirst();

        if (optionalSymbolic.isPresent()) {
            return checkTypeSame(symbolicProperties);
        } else {
            log.debug("No symbolics found");
            return true;
        }
    }

    private boolean checkTypeSame(List<SymbolicProperty> symbolicProperties) {
        if (symbolicProperties.isEmpty()) {
            return true;
        } else {
            String name = symbolicProperties.get(0).getMatDataTypeDescription();
            return symbolicProperties.stream()
                    .allMatch(s -> s.getMatDataTypeDescription().equals(name));
        }
    }

    private void convertIncludes() {
        includeProperties.forEach(this::processStandardLibrary);

        includeProperties.forEach(this::processIncludesCalled);

        includeProperties
                .forEach(this::setToFhir);
    }

    private void processStandardLibrary(IncludeProperties includeProperties) {

        String filteredName = StringUtils.remove(includeProperties.getName(), "_");
        String version = conversionLibLookupMap.get(filteredName);

        if (version != null) {
            includeProperties.setVersion(version);
            includeProperties.setName(filteredName);
        }
    }

    private void processIncludesCalled(IncludeProperties include) {
        var optional = isStandardLibrary(include.getName());

        include.setDisplay(optional.isEmpty());

        if (optional.isPresent() && StringUtils.isNotEmpty(include.getCalled())) {
            optional.get().setCalled(include.getCalled());
        }
    }

    private Optional<IncludeProperties> isStandardLibrary(String name) {
        return standardIncludeProperties.stream()
                .filter(s -> getNonFhirName(s.getName()).equals(name))
                .findFirst();
    }

    private String getNonFhirName(String name) {
        int idx = name.indexOf("FHIR4");

        if (idx < 0) {
            return name;
        } else {
            return name.substring(0, idx);
        }
    }

    private void convertUsing() {
        usingProperties = cqlTextParser.getUsing();
        setToFhir(usingProperties);
    }

    public void convertLibrary() {
        setToFhir(cqlTextParser.getLibrary());
    }

    public void setToFhir(BaseProperties properties) {
        properties.setToFhir();
        cqlTextParser.setToFhir(properties);
    }

    @Data
    public static class StandardLib {
        final String name;
        final String version;
        final String defaultCalled;
    }
}
