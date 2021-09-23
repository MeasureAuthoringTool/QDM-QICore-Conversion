package gov.cms.mat.qdmqicore.mapping.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.fhir.rest.dto.spreadsheet.CodeSystemEntry;
import gov.cms.mat.fhir.rest.dto.spreadsheet.ConversionAttributes;
import gov.cms.mat.fhir.rest.dto.spreadsheet.ConversionDataTypes;
import gov.cms.mat.fhir.rest.dto.spreadsheet.DataType;
import gov.cms.mat.fhir.rest.dto.spreadsheet.FhirLightBoxDatatypeAttributeAssociations;
import gov.cms.mat.fhir.rest.dto.spreadsheet.MatAttribute;
import gov.cms.mat.fhir.rest.dto.spreadsheet.QdmToQicoreMapping;
import gov.cms.mat.fhir.rest.dto.spreadsheet.RequiredMeasureField;
import gov.cms.mat.fhir.rest.dto.spreadsheet.ResourceDefinition;
import gov.cms.mat.qdmqicore.mapping.model.google.GoogleConversionAttributesData;
import gov.cms.mat.qdmqicore.mapping.model.google.GoogleConversionDataCodeSystemEntry;
import gov.cms.mat.qdmqicore.mapping.model.google.GoogleConversionDataTypesData;
import gov.cms.mat.qdmqicore.mapping.model.google.GoogleDataTypesData;
import gov.cms.mat.qdmqicore.mapping.model.google.GoogleFhirLightBoxDataTypesForFunctionArgsData;
import gov.cms.mat.qdmqicore.mapping.model.google.GoogleFhirLightBoxDatatypeAttributeAssociationData;
import gov.cms.mat.qdmqicore.mapping.model.google.GoogleMatAttributesData;
import gov.cms.mat.qdmqicore.mapping.model.google.GooglePopulationBasisValidValuesData;
import gov.cms.mat.qdmqicore.mapping.model.google.GoogleQdmToQicoreMappingData;
import gov.cms.mat.qdmqicore.mapping.model.google.GoogleRequiredFieldsData;
import gov.cms.mat.qdmqicore.mapping.model.google.GoogleResourceDefinitionData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import static gov.cms.mat.qdmqicore.mapping.utils.SpreadSheetUtils.commaDelimitedStringToList;
import static gov.cms.mat.qdmqicore.mapping.utils.SpreadSheetUtils.getData;

@Service
@Slf4j
public class GoogleSpreadsheetService {
    private static final String LOG_MESSAGE = "Received {} records from the spreadsheet's JSON, URL: {}";

    @Qualifier("externalRestTemplate")
    private final RestTemplate restTemplate;

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
    private ObjectMapper objectMapper;

    public GoogleSpreadsheetService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Cacheable("matAttributes")
    public List<MatAttribute> getMatAttributes() throws IOException {
        GoogleMatAttributesData data = objectMapper.readValue(new URL(matAttributesUrl), GoogleMatAttributesData.class);
//        GoogleMatAttributesData data = restTemplate.getForObject(matAttributesUrl, GoogleMatAttributesData.class);

        if (data != null && data.getFeed() != null && data.getFeed().getEntry() != null) {
            log.info(LOG_MESSAGE, data.getFeed().getEntry().size(), matAttributesUrl);
            return data.getFeed().getEntry().stream().map(e -> {
                var m = new MatAttribute();
                m.setDataTypeDescription(getData(e.getMatDataTypeDescription()));
                m.setDropDown(commaDelimitedStringToList(e.getDropDown()));
                m.setFhirQicoreMapping(getData(e.getFhirR4QiCoreMapping()));
                m.setFhirResource(getData(e.getFhirResource()));
                m.setFhirType(getData(e.getFhirType()));
                m.setHelpWording(getData(e.getHelpWording()));
                m.setFhirElement(getData(e.getFhirElement()));
                m.setMatAttributeName(getData(e.getMatAttributeName()));
                return m;
            }).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    @Cacheable("qdmToQicoreMapping")
    public List<QdmToQicoreMapping> getQdmToQicoreMapping() throws IOException {
        GoogleQdmToQicoreMappingData data = objectMapper.readValue(new URL(qdmQiCoreMappingUrl), GoogleQdmToQicoreMappingData.class);
//        GoogleQdmToQicoreMappingData data = restTemplate.getForObject(qdmQiCoreMappingUrl, GoogleQdmToQicoreMappingData.class);

        if (data != null && data.getFeed() != null && data.getFeed().getEntry() != null) {
            log.info(LOG_MESSAGE, data.getFeed().getEntry().size(), matAttributesUrl);
            return data.getFeed().getEntry().stream().map(e -> {
                var q = new QdmToQicoreMapping();
                q.setFhirQICoreMapping(getData(e.getFhir4QiCoreMapping()));
                q.setMatAttributeType(getData(e.getMatAttributeNameNormal()));
                q.setMatDataType(getData(e.getMatDataType()));
                q.setTitle(getData(e.getTitle()));
                q.setType(getData(e.getType()));
                q.setCardinality(getData(e.getCardinality()));
                return q;
            }).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    @Cacheable("dataTypes")
    public List<DataType> getDataTypes() throws IOException {
        GoogleDataTypesData data = objectMapper.readValue(new URL(dataTypesUrl), GoogleDataTypesData.class);
//        GoogleDataTypesData data = restTemplate.getForObject(dataTypesUrl, GoogleDataTypesData.class);

        if (data != null && data.getFeed() != null && data.getFeed().getEntry() != null) {
            log.info(LOG_MESSAGE, data.getFeed().getEntry().size(), matAttributesUrl);
            return data.getFeed().getEntry().stream().map(e -> {
                var d = new DataType();
                d.setDataType(getData(e.getDataType()));
                d.setRegex(getData(e.getRegex()));
                d.setType(getData(e.getType()));
                d.setValidValues(getData(e.getValidValues()));
                return d;
            }).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    @Cacheable("requiredMeasureFields")
    public List<RequiredMeasureField> getRequiredMeasureFields() throws IOException {
        GoogleRequiredFieldsData data = objectMapper.readValue(new URL(requiredMeasureFieldsUrl), GoogleRequiredFieldsData.class);
//        GoogleRequiredFieldsData data = restTemplate.getForObject(requiredMeasureFieldsUrl, GoogleRequiredFieldsData.class);

        if (data != null && data.getFeed() != null && data.getFeed().getEntry() != null) {
            log.info(LOG_MESSAGE, data.getFeed().getEntry().size(), matAttributesUrl);
            return data.getFeed().getEntry().stream().map(e -> {
                var r = new RequiredMeasureField();
                r.setField(getData(e.getField()));
                r.setType(getData(e.getType()));
                return r;
            }).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    @Cacheable("resourceDefinitions")
    public List<ResourceDefinition> getResourceDefinitions() throws IOException {
        GoogleResourceDefinitionData data = objectMapper.readValue(new URL(resourceDefinitionUrl), GoogleResourceDefinitionData.class);
//        GoogleResourceDefinitionData data = restTemplate.getForObject(resourceDefinitionUrl, GoogleResourceDefinitionData.class);

        if (data != null && data.getFeed() != null && data.getFeed().getEntry() != null) {
            log.info(LOG_MESSAGE, data.getFeed().getEntry().size(), matAttributesUrl);
            return data.getFeed().getEntry().stream().map(e -> {
                var r = new ResourceDefinition();
                r.setCardinality(getData(e.getCardinality()));
                r.setDefinition(getData(e.getDefinition()));
                r.setElementId(getData(e.getElementId()));
                r.setIsModifier(getData(e.getIsModifier()));
                r.setIsSummary(getData(e.getIsSummary()));
                r.setType(getData(e.getType()));
                return r;
            }).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }


    @Cacheable("conversionDataTypes")
    public List<ConversionDataTypes> getConversionDataTypes() throws IOException {
        GoogleConversionDataTypesData data = objectMapper.readValue(new URL(conversionDataTypesUrl), GoogleConversionDataTypesData.class);
//        GoogleConversionDataTypesData data = restTemplate.getForObject(conversionDataTypesUrl, GoogleConversionDataTypesData.class);

        if (data != null && data.getFeed() != null && data.getFeed().getEntry() != null) {
            log.info(LOG_MESSAGE, data.getFeed().getEntry().size(), conversionDataTypesUrl);
            return data.getFeed().getEntry().stream().map(e -> {
                var r = new ConversionDataTypes();
                r.setFhirType(getData(e.getFhirType()));
                r.setQdmType(getData(e.getQdmType()));
                r.setWhereAdjustment(getData(e.getWhereAdjustment()));
                r.setComment(getData(e.getComment()));
                return r;
            }).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }


    @Cacheable("conversionAttributes")
    public List<ConversionAttributes> getConversionAttributes() throws IOException {
        GoogleConversionAttributesData data = objectMapper.readValue(new URL(attributesUrl), GoogleConversionAttributesData.class);
//        GoogleConversionAttributesData data = restTemplate.getForObject(attributesUrl, GoogleConversionAttributesData.class);

        if (data != null && data.getFeed() != null && data.getFeed().getEntry() != null) {
            log.info(LOG_MESSAGE, data.getFeed().getEntry().size(), attributesUrl);
            return data.getFeed().getEntry().stream().map(e -> {
                var r = new ConversionAttributes();
                r.setFhirType(getData(e.getFhirType()));
                r.setQdmType(getData(e.getQdmType()));
                r.setFhirAttribute(getData(e.getFhirAttribute()));
                r.setQdmAttribute(getData(e.getQdmAttribute()));
                r.setComment(getData(e.getComment()));
                return r;
            }).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    public List<FhirLightBoxDatatypeAttributeAssociations> getFhirLightBoxDatatypeAttributeAssociation() throws IOException {
        GoogleFhirLightBoxDatatypeAttributeAssociationData data = objectMapper.readValue(new URL(fhirLightboxDatatypeAttributeAssociationUrl), GoogleFhirLightBoxDatatypeAttributeAssociationData.class);
//        GoogleFhirLightBoxDatatypeAttributeAssociationData
//                data = restTemplate.getForObject(fhirLightboxDatatypeAttributeAssociationUrl, GoogleFhirLightBoxDatatypeAttributeAssociationData.class);

        if (data != null && data.getFeed() != null && data.getFeed().getEntry() != null) {
            log.info(LOG_MESSAGE, data.getFeed().getEntry().size(), attributesUrl);
            return data.getFeed().getEntry().stream().map(e -> {
                var r = new FhirLightBoxDatatypeAttributeAssociations();
                r.setDatatype(getData(e.getDatatype()));
                r.setAttribute(getData(e.getAttribute()));
                r.setAttributeType(getData(e.getAttributeType()));

                String hasBinding = getData(e.getHasBinding());

                if (StringUtils.isNotBlank(hasBinding)) {
                    r.setHasBinding(Boolean.parseBoolean(hasBinding));
                }

                return r;
            }).sorted().collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    @Cacheable("fhirLightboxDataTypesForFunctionArgs")
    public List<String> getFhirLightboxDataTypesForFunctionArgs() throws IOException {
        GoogleFhirLightBoxDataTypesForFunctionArgsData data = objectMapper.readValue(new URL(fhirLightboxDataTypesForFunctionArgsUrl), GoogleFhirLightBoxDataTypesForFunctionArgsData.class);
//        GoogleFhirLightBoxDataTypesForFunctionArgsData
//                data = restTemplate.getForObject(fhirLightboxDataTypesForFunctionArgsUrl, GoogleFhirLightBoxDataTypesForFunctionArgsData.class);

        if (data != null && data.getFeed() != null && data.getFeed().getEntry() != null) {
            log.info(LOG_MESSAGE, data.getFeed().getEntry().size(), fhirLightboxDatatypeAttributeAssociationUrl);

            return data.getFeed().getEntry().stream()
                    .map(e -> getData(e.getDatatype()))
                    .sorted(String::compareToIgnoreCase)
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    @Cacheable("populationBasisValidValues")
    public Collection<String> getPopulationBasisValidValues() throws IOException {
        GooglePopulationBasisValidValuesData data = objectMapper.readValue(new URL(populationBasisValidValuesUrl), GooglePopulationBasisValidValuesData.class);
//        GooglePopulationBasisValidValuesData
//                data = restTemplate.getForObject(populationBasisValidValuesUrl, GooglePopulationBasisValidValuesData.class);

        if (data != null && data.getFeed() != null && data.getFeed().getEntry() != null) {
            log.info(LOG_MESSAGE, data.getFeed().getEntry().size(), fhirLightboxDatatypeAttributeAssociationUrl);

            return data.getFeed().getEntry().stream()
                    .map(e -> getData(e.getBaseResource()))
                    .sorted()
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        } else {
            return Collections.emptyList();
        }
    }

    public List<CodeSystemEntry> getCodeSystemEntries() throws IOException {
        CodeSystemEntry[] data = objectMapper.readValue(new URL(codeSystemEntryUrl), CodeSystemEntry[].class);

        if(data != null) {
            return Arrays.asList(data);
        }

        return Collections.emptyList();
    }
}
