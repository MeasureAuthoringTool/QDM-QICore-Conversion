package gov.cms.mat.qdmqicore.mapping.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.fhir.rest.dto.spreadsheet.*;
import gov.cms.mat.qdmqicore.mapping.model.google.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GoogleSpreadsheetService {
    private static final String LOG_MESSAGE = "Received {} records from the spreadsheet's JSON, URL: {}";

    @Value("${json.data.mat-attributes-url}")
    private String matAttributesUrl;
    @Value("${json.data.qdm-qi-core-mapping-url}")
    private String qdmQiCoreMappingUrl;
    @Value("${json.data.data-types-url}")
    private String dataTypesUrl;
    @Value("${json.data.required-measure-fields-url}")
    private String requiredMeasureFieldsUrl;
    @Value("${json.data.resource-definition-url}")
    private String resourceDefinitionUrl;
    @Value("${json.data.conversion-data-types-url}")
    private String conversionDataTypesUrl;
    @Value("${json.data.attributes-url}")
    private String attributesUrl;
    @Value("${json.data.fhir-lightbox-datatype_attribute_association-url}")
    private String fhirLightboxDatatypeAttributeAssociationUrl;
    @Value("${json.data.fhir-lightbox-datatype_for_function_args-url}")
    private String fhirLightboxDataTypesForFunctionArgsUrl;
    @Value("${json.data.population-basis-valid-values-url}")
    private String populationBasisValidValuesUrl;
    @Value("${json.data.code-system-entry-url}")
    private String codeSystemEntryUrl;

    @Autowired
    private final ObjectMapper objectMapper;

    public GoogleSpreadsheetService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Cacheable("matAttributes")
    public List<MatAttribute> getMatAttributes() throws IOException {
        List<MatAttributes> data = objectMapper.readValue(new URL(matAttributesUrl), new TypeReference<>() {});

        if (data != null) {
            log.info(LOG_MESSAGE, data.size(), matAttributesUrl);
            return data.stream().map(e -> {
                var m = new MatAttribute();
                m.setDataTypeDescription(e.getDataTypeDescription());
                m.setMatAttributeName(e.getMatAttributeName());
                m.setFhirQicoreMapping(e.getFhirQicoreMapping());
                m.setFhirResource(e.getFhirResource());
                m.setFhirType(e.getFhirType());
                m.setFhirElement(e.getFhirElement());
                m.setHelpWording(e.getHelpWording());
                m.setDropDown(e.getDropDown());
                return m;
            }).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    @Cacheable("qdmToQicoreMapping")
    public List<QdmToQicoreMapping> getQdmToQicoreMapping() throws IOException {
        List<QdmToQicoreMappings> data = objectMapper.readValue(new URL(qdmQiCoreMappingUrl), new TypeReference<>() {});

        if (data != null) {
            log.info(LOG_MESSAGE, data.size(), qdmQiCoreMappingUrl);
            return data.stream().map(e -> {
                var q = new QdmToQicoreMapping();
                q.setTitle(e.getTitle());
                q.setMatAttributeType(e.getMatAttributeType());
                q.setFhirQICoreMapping(e.getFhirQICoreMapping());
                q.setType(e.getType());
                q.setCardinality(e.getCardinality());
                return q;
            }).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    @Cacheable("dataTypes")
    public List<DataType> getDataTypes() throws IOException {
        List<DataTypes> data = objectMapper.readValue(new URL(dataTypesUrl), new TypeReference<>() {});

        if (data != null) {
            log.info(LOG_MESSAGE, data.size(), dataTypesUrl);
            return data.stream().map(e -> {
                var d = new DataType();
                d.setDataType(e.getDataType());
                d.setValidValues(e.getValidValues());
                d.setRegex(e.getRegex());
                d.setType(e.getType());
                return d;
            }).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    @Cacheable("requiredMeasureFields")
    public List<RequiredMeasureField> getRequiredMeasureFields() throws IOException {
        List<RequiredFields> data = objectMapper.readValue(new URL(requiredMeasureFieldsUrl), new TypeReference<>() {});

        if (data != null) {
            log.info(LOG_MESSAGE, data.size(), requiredMeasureFieldsUrl);
            return data.stream().map(e -> {
                var r = new RequiredMeasureField();
                r.setField(e.getField());
                r.setType(e.getType());
                return r;
            }).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    @Cacheable("resourceDefinitions")
    public List<ResourceDefinition> getResourceDefinitions() throws IOException {
        List<ResourceDefinitions> data = objectMapper.readValue(new URL(resourceDefinitionUrl), new TypeReference<>() {});

        if (data != null) {
            log.info(LOG_MESSAGE, data.size(), resourceDefinitionUrl);
            return data.stream().map(e -> {
                var r = new ResourceDefinition();
                r.setElementId(e.getElementId());
                r.setDefinition(e.getDefinition());
                r.setCardinality(e.getCardinality());
                r.setType(e.getType());
                r.setIsSummary(e.getIsSummary());
                r.setIsModifier(e.getIsModifier());
                return r;
            }).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    @Cacheable("conversionDataTypes")
    public List<ConversionDataTypes> getConversionDataTypes() throws IOException {
        List<ConversionDataType> data = objectMapper.readValue(new URL(conversionDataTypesUrl), new TypeReference<>() {});

        if (data != null) {
            log.info(LOG_MESSAGE, data.size(), conversionDataTypesUrl);
            return data.stream().map(e -> {
                var r = new ConversionDataTypes();
                r.setFhirType(e.getFhirType());
                r.setQdmType(e.getQdmType());
                r.setWhereAdjustment(e.getWhereAdjustment());
                r.setComment(e.getComment());
                return r;
            }).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    @Cacheable("conversionAttributes")
    public List<ConversionAttributes> getConversionAttributes() throws IOException {
        List<ConversionAttribute> data = objectMapper.readValue(new URL(attributesUrl), new TypeReference<>() {});

        if (data != null) {
            log.info(LOG_MESSAGE, data.size(), attributesUrl);
            return data.stream().map(e -> {
                var r = new ConversionAttributes();
                r.setFhirType(e.getFhirType());
                r.setQdmType(e.getQdmType());
                r.setFhirAttribute(e.getFhirAttribute());
                r.setQdmAttribute(e.getQdmAttribute());
                r.setComment(e.getComment());
                return r;
            }).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    @Cacheable("fhirLightBoxDatatypeAttributeAssociation")
    public List<FhirLightBoxDatatypeAttributeAssociations> getFhirLightBoxDatatypeAttributeAssociation() throws IOException {
        List<FhirLightBoxDatatypeAttributeAssociationTypes> data = objectMapper.readValue(new URL(fhirLightboxDatatypeAttributeAssociationUrl), new TypeReference<>() {});

        if (data != null) {
            log.info(LOG_MESSAGE, data.size(), fhirLightboxDatatypeAttributeAssociationUrl);
            return data.stream().map(e -> {
                var r = new FhirLightBoxDatatypeAttributeAssociations();
                r.setDatatype(e.getDataType());
                r.setAttribute(e.getAttribute());
                r.setAttributeType(e.getAttributeType());
                r.setHasBinding(e.getHasBinding());
                return r;
            }).sorted().collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    @Cacheable("fhirLightboxDataTypesForFunctionArgs")
    public List<String> getFhirLightboxDataTypesForFunctionArgs() throws IOException {
        List<String> data = objectMapper.readValue(new URL(fhirLightboxDataTypesForFunctionArgsUrl), new TypeReference<>() {});

        if (data != null) {
            log.info(LOG_MESSAGE, data.size(), fhirLightboxDataTypesForFunctionArgsUrl);
            return data.stream()
                    .sorted(String::compareToIgnoreCase)
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    @Cacheable("populationBasisValidValues")
    public List<String> getPopulationBasisValidValues() throws IOException {
        List<String> data = objectMapper.readValue(new URL(populationBasisValidValuesUrl), new TypeReference<>() {});

        if (data != null) {
            log.info(LOG_MESSAGE, data.size(), populationBasisValidValuesUrl);
            return data.stream()
                    .sorted()
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    @Cacheable("codeSystemEntries")
    public List<CodeSystemEntry> getCodeSystemEntries() throws IOException {
        CodeSystemEntry[] data = objectMapper.readValue(new URL(codeSystemEntryUrl), CodeSystemEntry[].class);

        if(data != null) {
            return Arrays.asList(data);
        }

        return Collections.emptyList();
    }
}
