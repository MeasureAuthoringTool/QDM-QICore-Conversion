package gov.cms.mat.qdmqicore.mapping.controller;

import gov.cms.mat.fhir.rest.dto.spreadsheet.*;
import gov.cms.mat.qdmqicore.mapping.service.GoogleSpreadsheetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(path = "/")
@Slf4j
public class GoogleSpreadsheetController {
    private final GoogleSpreadsheetService spreadsheetService;

    public GoogleSpreadsheetController(GoogleSpreadsheetService spreadsheetService) {
        this.spreadsheetService = spreadsheetService;
    }

    @GetMapping(path = "/matAttributes")
    public List<MatAttribute> matAttributes() throws IOException {
        return spreadsheetService.getMatAttributes();
    }

    @GetMapping(path = "/qdmToQicoreMappings")
    public List<QdmToQicoreMapping> qdmToQicoreMappings() throws IOException {
        return spreadsheetService.getQdmToQicoreMapping();
    }

    @GetMapping(path = "/dataTypes")
    public List<DataType> dataTypes() throws IOException {
        return spreadsheetService.getDataTypes();
    }

    @GetMapping(path = "/requiredMeasureFields")
    public List<RequiredMeasureField> requiredMeasureFields() throws IOException {
        return spreadsheetService.getRequiredMeasureFields();
    }

    @GetMapping(path = "/resourceDefinition")
    public List<ResourceDefinition> resourceDefinition() throws IOException {
        return spreadsheetService.getResourceDefinitions();
    }

    @GetMapping(path = "/conversionDataTypes")
    public List<ConversionDataTypes> conversionDataTypes() throws IOException {
        return spreadsheetService.getConversionDataTypes();
    }

    @GetMapping(path = "/conversionAttributes")
    public List<ConversionAttributes> conversionAttributes() throws IOException {
        return spreadsheetService.getConversionAttributes();
    }

    @GetMapping(path = "/fhirLightBoxDatatypeAttributeAssociation")
    public List<FhirLightBoxDatatypeAttributeAssociations> fhirLightBoxDatatypeAttributeAssociation() throws IOException {
        return spreadsheetService.getFhirLightBoxDatatypeAttributeAssociation();
    }

    @GetMapping(path = "/fhirLightboxDataTypesForFunctionArgs")
    public List<String> fhirLightboxDataTypesForFunctionArgs() throws IOException {
        return spreadsheetService.getFhirLightboxDataTypesForFunctionArgs();
    }

    @GetMapping(path = "/populationBasisValidValues")
    public List<String> populationBasisValidValues() throws IOException {
        return spreadsheetService.getPopulationBasisValidValues();
    }

    @GetMapping(path = "/codeSystemEntries")
    public List<CodeSystemEntry> codeSystemEntries() throws IOException {
        return spreadsheetService.getCodeSystemEntries();
    }
}
