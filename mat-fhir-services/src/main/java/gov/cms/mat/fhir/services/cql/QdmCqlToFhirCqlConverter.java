package gov.cms.mat.fhir.services.cql;

import gov.cms.mat.cql.CqlParser;
import gov.cms.mat.cql.elements.*;
import gov.cms.mat.fhir.rest.dto.ConversionMapping;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.service.QdmQiCoreDataService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class QdmCqlToFhirCqlConverter {

    public static final String STD_FHIR_LIBS = "\ninclude FHIRHelpers version '4.0.0'\n" +
            "include SupplementalDataElements_FHIR4 version '1.0.0'\n" +
            "include MATGlobalCommonFunctions_FHIR4 version '4.0.000'\n";


    private static final String ERROR_MESSAGE =
            "DEFINE crosses dissimilar FHIR Resources within UNION statements, this will fail processing, " +
                    "consider creating define statements limited to single FHIR Resource.";
    private final CqlParser cqlParser;
    private final QdmQiCoreDataService qdmQiCoreDataService;

    private UsingProperties usingProperties;

    public QdmCqlToFhirCqlConverter(String cqlText, QdmQiCoreDataService qdmQiCoreDataService) {

        cqlParser = new CqlParser(cqlText);
        this.qdmQiCoreDataService = qdmQiCoreDataService;
    }

    public String convert(String matLibId) {
        convertLibrary();
        convertUsing();
        convertIncludes();
        convertDefines();

        checkUnion(matLibId);

        return addDefaultFhirLibraries();
    }

    private String addDefaultFhirLibraries() {
        String cqlUsingLine = usingProperties.createCql();
        String cqlReplacement = cqlUsingLine + STD_FHIR_LIBS;

        return cqlParser.getCql()
                .replace(cqlUsingLine, cqlReplacement);
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
        cqlParser.getIncludes()
                .forEach(this::setToFhir);
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
}
