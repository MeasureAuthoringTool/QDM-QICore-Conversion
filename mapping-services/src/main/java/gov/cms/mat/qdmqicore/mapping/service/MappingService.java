package gov.cms.mat.qdmqicore.mapping.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.fhir.rest.dto.spreadsheet.*;
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
public class MappingService {
    private static final String LOG_MESSAGE = "Received {} records from the JSON, URL: {}";

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

    public MappingService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Cacheable("matAttributes")
    public List<MatAttribute> getMatAttributes() throws IOException {
        List<MatAttribute> data = objectMapper.readValue(new URL(matAttributesUrl), new TypeReference<>() {});

        if (data != null) {
            log.info(LOG_MESSAGE, data.size(), matAttributesUrl);
            return data;
        } else {
            return Collections.emptyList();
        }
    }

    @Cacheable("qdmToQicoreMapping")
    public List<QdmToQicoreMapping> getQdmToQicoreMapping() throws IOException {
        List<QdmToQicoreMapping> data = objectMapper.readValue(new URL(qdmQiCoreMappingUrl), new TypeReference<>() {});

        if (data != null) {
            log.info(LOG_MESSAGE, data.size(), qdmQiCoreMappingUrl);
            return data;
        } else {
            return Collections.emptyList();
        }
    }

    @Cacheable("dataTypes")
    public List<DataType> getDataTypes() throws IOException {
        List<DataType> data = objectMapper.readValue(new URL(dataTypesUrl), new TypeReference<>() {});

        if (data != null) {
            log.info(LOG_MESSAGE, data.size(), dataTypesUrl);
            return data;
        } else {
            return Collections.emptyList();
        }
    }

    @Cacheable("requiredMeasureFields")
    public List<RequiredMeasureField> getRequiredMeasureFields() throws IOException {
        List<RequiredMeasureField> data = objectMapper.readValue(new URL(requiredMeasureFieldsUrl), new TypeReference<>() {});

        if (data != null) {
            log.info(LOG_MESSAGE, data.size(), requiredMeasureFieldsUrl);
            return data;
        } else {
            return Collections.emptyList();
        }
    }

    @Cacheable("resourceDefinitions")
    public List<ResourceDefinition> getResourceDefinitions() throws IOException {
        List<ResourceDefinition> data = objectMapper.readValue(new URL(resourceDefinitionUrl), new TypeReference<>() {});

        if (data != null) {
            log.info(LOG_MESSAGE, data.size(), resourceDefinitionUrl);
            return data;
        } else {
            return Collections.emptyList();
        }
    }

    @Cacheable("conversionDataTypes")
    public List<ConversionDataTypes> getConversionDataTypes() throws IOException {
        List<ConversionDataTypes> data = objectMapper.readValue(new URL(conversionDataTypesUrl), new TypeReference<>() {});

        if (data != null) {
            log.info(LOG_MESSAGE, data.size(), conversionDataTypesUrl);
            return data;
        } else {
            return Collections.emptyList();
        }
    }

    @Cacheable("conversionAttributes")
    public List<ConversionAttributes> getConversionAttributes() throws IOException {
        List<ConversionAttributes> data = objectMapper.readValue(new URL(attributesUrl), new TypeReference<>() {});

        if (data != null) {
            log.info(LOG_MESSAGE, data.size(), attributesUrl);
            return data;
        } else {
            return Collections.emptyList();
        }
    }

    @Cacheable("fhirLightBoxDatatypeAttributeAssociation")
    public List<FhirLightBoxDatatypeAttributeAssociations> getFhirLightBoxDatatypeAttributeAssociation() throws IOException {
        List<FhirLightBoxDatatypeAttributeAssociations> data = objectMapper.readValue(new URL(fhirLightboxDatatypeAttributeAssociationUrl), new TypeReference<>() {});

        if (data != null) {
            log.info(LOG_MESSAGE, data.size(), fhirLightboxDatatypeAttributeAssociationUrl);
            return data.stream().sorted().collect(Collectors.toList());
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
            // data is sorted by considering case, since FHIR default has to be boolean and should be at the end in the list(UI).
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
