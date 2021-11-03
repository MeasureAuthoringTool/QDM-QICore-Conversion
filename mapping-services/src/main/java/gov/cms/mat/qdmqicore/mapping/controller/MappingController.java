package gov.cms.mat.qdmqicore.mapping.controller;

import gov.cms.mat.fhir.rest.dto.spreadsheet.*;
import gov.cms.mat.qdmqicore.mapping.service.MappingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(path = "/")
@Slf4j
public class MappingController {
    private final MappingService mappingService;

    public MappingController(MappingService mappingService) {
        this.mappingService = mappingService;
    }

    @GetMapping(path = "/matAttributes")
    public List<MatAttribute> matAttributes() throws IOException {
        return mappingService.getMatAttributes();
    }

    @GetMapping(path = "/qdmToQicoreMappings")
    public List<QdmToQicoreMapping> qdmToQicoreMappings() throws IOException {
        return mappingService.getQdmToQicoreMapping();
    }

    @GetMapping(path = "/dataTypes")
    public List<DataType> dataTypes() throws IOException {
        return mappingService.getDataTypes();
    }

    @GetMapping(path = "/requiredMeasureFields")
    public List<RequiredMeasureField> requiredMeasureFields() throws IOException {
        return mappingService.getRequiredMeasureFields();
    }

    @GetMapping(path = "/resourceDefinition")
    public List<ResourceDefinition> resourceDefinition() throws IOException {
        return mappingService.getResourceDefinitions();
    }

    @GetMapping(path = "/conversionDataTypes")
    public List<ConversionDataTypes> conversionDataTypes() throws IOException {
        return mappingService.getConversionDataTypes();
    }

    @GetMapping(path = "/conversionAttributes")
    public List<ConversionAttributes> conversionAttributes() throws IOException {
        return mappingService.getConversionAttributes();
    }

    @GetMapping(path = "/fhirLightBoxDatatypeAttributeAssociation")
    public List<FhirLightBoxDatatypeAttributeAssociations> fhirLightBoxDatatypeAttributeAssociation() throws IOException {
        return mappingService.getFhirLightBoxDatatypeAttributeAssociation();
    }

    @GetMapping(path = "/fhirLightboxDataTypesForFunctionArgs")
    public List<String> fhirLightboxDataTypesForFunctionArgs() throws IOException {
        return mappingService.getFhirLightboxDataTypesForFunctionArgs();
    }

    @GetMapping(path = "/populationBasisValidValues")
    public List<String> populationBasisValidValues() throws IOException {
        return mappingService.getPopulationBasisValidValues();
    }

    @GetMapping(path = "/codeSystemEntries")
    public List<CodeSystemEntry> codeSystemEntries() throws IOException {
        return mappingService.getCodeSystemEntries();
    }
}
