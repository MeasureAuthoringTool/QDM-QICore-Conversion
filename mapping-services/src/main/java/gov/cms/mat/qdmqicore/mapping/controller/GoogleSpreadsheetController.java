package gov.cms.mat.qdmqicore.mapping.controller;

import gov.cms.mat.fhir.rest.dto.spreadsheet.CodeSystemEntry;
import gov.cms.mat.fhir.rest.dto.spreadsheet.ConversionAttributes;
import gov.cms.mat.fhir.rest.dto.spreadsheet.ConversionDataTypes;
import gov.cms.mat.fhir.rest.dto.spreadsheet.DataType;
import gov.cms.mat.fhir.rest.dto.spreadsheet.FhirLightBoxDatatypeAttributeAssociations;
import gov.cms.mat.fhir.rest.dto.spreadsheet.MatAttribute;
import gov.cms.mat.fhir.rest.dto.spreadsheet.QdmToQicoreMapping;
import gov.cms.mat.fhir.rest.dto.spreadsheet.RequiredMeasureField;
import gov.cms.mat.fhir.rest.dto.spreadsheet.ResourceDefinition;
import gov.cms.mat.qdmqicore.mapping.service.GoogleSpreadsheetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
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
    public List<MatAttribute> matAttributes() {
        return spreadsheetService.getMatAttributes();
    }

    @GetMapping(path = "/qdmToQicoreMappings")
    public List<QdmToQicoreMapping> qdmToQicoreMappings() {
        return spreadsheetService.getQdmToQicoreMapping();
    }

    @GetMapping(path = "/dataTypes")
    public List<DataType> dataTypes() {
        return spreadsheetService.getDataTypes();
    }

    @GetMapping(path = "/requiredMeasureFields")
    public List<RequiredMeasureField> requiredMeasureFields() {
        return spreadsheetService.getRequiredMeasureFields();
    }

    @GetMapping(path = "/resourceDefinition")
    public List<ResourceDefinition> resourceDefinition() {
        return spreadsheetService.getResourceDefinitions();
    }

    @GetMapping(path = "/conversionDataTypes")
    public List<ConversionDataTypes> conversionDataTypes() {
        return spreadsheetService.getConversionDataTypes();
    }

    @GetMapping(path = "/conversionAttributes")
    public List<ConversionAttributes> conversionAttributes() {
        return spreadsheetService.getConversionAttributes();
    }

    @GetMapping(path = "/fhirLightBoxDatatypeAttributeAssociation")
    public List<FhirLightBoxDatatypeAttributeAssociations> fhirLightBoxDatatypeAttributeAssociation() {
        return spreadsheetService.getFhirLightBoxDatatypeAttributeAssociation();
    }

    @GetMapping(path = "/fhirLightboxDataTypesForFunctionArgs")
    public List<String> fhirLightboxDataTypesForFunctionArgs() {
        return spreadsheetService.getFhirLightboxDataTypesForFunctionArgs();
    }

    @GetMapping(path = "/populationBasisValidValues")
    public Collection<String> populationBasisValidValues() {
        return spreadsheetService.getPopulationBasisValidValues();
    }

    @GetMapping(path = "/codeSystemEntries")
    public List<CodeSystemEntry> codeSystemEntries() {
        return spreadsheetService.getCodeSystemEntries();
    }
}
