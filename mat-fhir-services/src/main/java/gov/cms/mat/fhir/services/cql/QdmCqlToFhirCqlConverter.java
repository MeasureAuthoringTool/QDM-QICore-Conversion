package gov.cms.mat.fhir.services.cql;

import gov.cms.mat.cql.CqlParser;
import gov.cms.mat.cql.elements.*;
import gov.cms.mat.fhir.rest.dto.ConversionMapping;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.service.QdmQiCoreDataService;
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
            new StandardLib("FHIRHelpers", "4.0.001", "FHIRHelpers"),
            new StandardLib("SupplementalDataElements_FHIR4", "2.0.000", "SDE"),
            new StandardLib("MATGlobalCommonFunctions_FHIR4", "5.0.000", "Global")
    };

    private static final String ERROR_MESSAGE =
            "DEFINE crosses dissimilar FHIR Resources within UNION statements, this will fail processing, " +
                    "consider creating define statements limited to single FHIR Resource.";
    private final CqlParser cqlParser;
    private final QdmQiCoreDataService qdmQiCoreDataService;
    private final Map<String, String> conversionLibLookupMap;
    private final Map<String, String> codeSystemLookupMap;
    private final HapiFhirServer hapiFhirServer;

    List<IncludeProperties> includeProperties;
    List<IncludeProperties> standardIncludeProperties;
    private UsingProperties usingProperties;

    public QdmCqlToFhirCqlConverter(String cqlText,
                                    QdmQiCoreDataService qdmQiCoreDataService,
                                    Map<String, String> conversionLibLookupMap,
                                    Map<String, String> codeSystemLookupMap,
                                    HapiFhirServer hapiFhirServer) {
        cqlParser = new CqlParser(cqlText);
        this.qdmQiCoreDataService = qdmQiCoreDataService;
        this.conversionLibLookupMap = conversionLibLookupMap;
        this.codeSystemLookupMap = codeSystemLookupMap;
        this.hapiFhirServer = hapiFhirServer;
        standardIncludeProperties = createStandardIncludes();
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
        includeProperties = cqlParser.getIncludes();

        convertLibrary();
        convertUsing();
        convertIncludes();
        convertDefines();
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
        List<ValueSetProperties> properties = cqlParser.getValueSets();
        properties.forEach(this::setToFhir);
    }

    private void convertCodeSystems() {
        List<CodeSystemProperties> properties = cqlParser.getCodeSystems();

        properties.forEach(this::processCodeSystem);

        properties.forEach(this::setToFhir);
    }

    private void processCodeSystem(CodeSystemProperties codeSystemProperties) {

        if (StringUtils.isNotEmpty(codeSystemProperties.getVersion())) {
            log.debug("Code system has version : {}, so not processing", codeSystemProperties.getVersion());
        } else {
            if (!processCodeSystemInGlobalMap(codeSystemProperties)) {
                processCodeSystemFhir(codeSystemProperties);
            }
        }
    }

    private void processCodeSystemFhir(CodeSystemProperties codeSystemProperties) {
        String oid = codeSystemProperties.getUrnOid();
        Bundle bundle = hapiFhirServer.getCodeSystemBundle(oid);

        if (bundle.hasEntry()) {
            Bundle.BundleEntryComponent bundleEntryComponent = bundle.getEntry().get(0);
            Resource resource = bundleEntryComponent.getResource();

            if (resource instanceof CodeSystem) {
                CodeSystem codeSystem = (CodeSystem) resource;
                log.debug("Code system {} set to CodeSystem URL: {}", codeSystemProperties.getName(), codeSystem.getUrl());
                codeSystemProperties.setUrnOid(codeSystem.getUrl());
            }

        } else {
            log.info("NOT found with oid: {}", oid);

        }
    }

    private boolean processCodeSystemInGlobalMap(CodeSystemProperties codeSystemProperties) {
        String name = codeSystemProperties.getName();

        String codeSystemReference = codeSystemLookupMap.get(name);

        if (StringUtils.isEmpty(codeSystemReference)) {
            log.debug("Cannot find code system with name: {}", name);
            return false;
        } else {
            log.debug("Found code system with name: {} reference: {}", name, codeSystemProperties);
            codeSystemProperties.setUrnOid(codeSystemReference);
            return true;
        }

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
        String cqlReplacement = cqlUsingLine + createStandardIncludesCql();

        String cql = cqlParser.getCql()
                .replace(cqlUsingLine, cqlReplacement);

        return cleanLines(cql);
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
        List<UnionProperties> unions = cqlParser.getUnions();
        var optional = unions.stream().filter(u -> !checkUnion(u)).findFirst();

        if (optional.isPresent()) {
            log.debug("We have library union issue");

            if (matLibId != null) {
                ConversionReporter.setCqlConversionErrorMessage(ERROR_MESSAGE, matLibId);
            }
        }
    }

    private boolean checkUnion(UnionProperties unionProperties) {
        List<SymbolicProperty> symbolicProperties = cqlParser.getSymbolicProperties(unionProperties.getLines());

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

    private void convertDefines() {
        List<DefineProperties> properties = cqlParser.getDefines();
        properties.forEach(this::processSymbolics);
        properties.forEach(this::setToFhir);
    }

    private void processSymbolics(DefineProperties properties) {
        properties.getSymbolicProperties()
                .forEach(this::processSymbolic);
    }

    private void processSymbolic(SymbolicProperty symbolicProperty) {
        List<ConversionMapping> conversionMappings =
                qdmQiCoreDataService.findAllFilteredByMatDataTypeDescription(symbolicProperty.getMatDataTypeDescription());
        symbolicProperty.setConversionMappings(conversionMappings);
    }

    private void convertIncludes() {
        includeProperties.forEach(this::processStandardLibrary);

        includeProperties.forEach(this::processIncludesCalled);

        includeProperties
                .forEach(this::setToFhir);
    }

    private void processStandardLibrary(IncludeProperties includeProperties) {

        String version = conversionLibLookupMap.get(includeProperties.getName());

        if (version != null) {
            includeProperties.setVersion(version);
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
        int idx = name.indexOf("_FHIR4");

        if (idx < 0) {
            return name;
        } else {
            return name.substring(0, idx);
        }
    }

    private void convertUsing() {
        usingProperties = cqlParser.getUsing();
        setToFhir(usingProperties);
    }

    public void convertLibrary() {
        setToFhir(cqlParser.getLibrary());
    }

    public void setToFhir(BaseProperties properties) {
        properties.setToFhir();
        cqlParser.setToFhir(properties);
    }

    @Data
    static class StandardLib {
        final String name;
        final String version;
        final String defaultCalled;
    }
}
