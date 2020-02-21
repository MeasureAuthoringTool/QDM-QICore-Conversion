package gov.cms.mat.fhir.services.cql;

import gov.cms.mat.cql.CqlParser;
import gov.cms.mat.cql.elements.BaseProperties;
import gov.cms.mat.cql.elements.DefineProperties;
import gov.cms.mat.cql.elements.SymbolicProperty;
import gov.cms.mat.fhir.rest.dto.ConversionMapping;
import gov.cms.mat.fhir.services.service.QdmQiCoreDataService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class QdmCqlToFhirCqlConverter {
    private final CqlParser cqlParser;
    private final QdmQiCoreDataService qdmQiCoreDataService;

    public QdmCqlToFhirCqlConverter(String cqlText, QdmQiCoreDataService qdmQiCoreDataService) {
        cqlParser = new CqlParser(cqlText);
        this.qdmQiCoreDataService = qdmQiCoreDataService;
    }

    public String convert() {
        convertLibrary();
        convertUsing();
        convertIncludes();
        convertDefines();

        return cqlParser.getCql();
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
        setToFhir(cqlParser.getUsing());
    }

    public void convertLibrary() {
        setToFhir(cqlParser.getLibrary());
    }

    public void setToFhir(BaseProperties properties) {
        properties.setToFhir();
        cqlParser.setToFhir(properties);
    }
}
