package gov.cms.mat.fhir.services.cql;

import gov.cms.mat.cql.CqlParser;
import gov.cms.mat.cql.elements.*;
import gov.cms.mat.fhir.rest.dto.ConversionMapping;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.service.QdmQiCoreDataService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class QdmCqlToFhirCqlConverter {
    private static final StandardLib[] STANDARD_LIBS = {
            new StandardLib("FHIRHelpers", "4.0.0"),
            new StandardLib("SupplementalDataElements_FHIR4", "1.0.0"),
            new StandardLib("MATGlobalCommonFunctions_FHIR4", "4.0.000")
    };

    private static final String ERROR_MESSAGE =
            "DEFINE crosses dissimilar FHIR Resources within UNION statements, this will fail processing, " +
                    "consider creating define statements limited to single FHIR Resource.";
    private final CqlParser cqlParser;
    private final QdmQiCoreDataService qdmQiCoreDataService;
    List<IncludeProperties> includeProperties;
    List<IncludeProperties> standardIncludeProperties;
    private UsingProperties usingProperties;

    public QdmCqlToFhirCqlConverter(String cqlText, QdmQiCoreDataService qdmQiCoreDataService) {

        cqlParser = new CqlParser(cqlText);
        this.qdmQiCoreDataService = qdmQiCoreDataService;
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
                .build();
    }

    public String convert(String matLibId) {
        includeProperties = cqlParser.getIncludes();

        convertLibrary();
        convertUsing();
        convertIncludes();
        convertDefines();

        checkUnion(matLibId);

        return addDefaultFhirLibraries();
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
        includeProperties.forEach(this::processIncludesCalled);

        includeProperties
                .forEach(this::setToFhir);
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
    }
}
