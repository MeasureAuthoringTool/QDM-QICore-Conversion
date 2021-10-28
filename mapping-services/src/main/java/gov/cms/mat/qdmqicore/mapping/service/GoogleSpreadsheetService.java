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
import gov.cms.mat.qdmqicore.mapping.model.google.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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

    private final ObjectMapper objectMapper;

    public GoogleSpreadsheetService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Cacheable("matAttributes")
    public List<MatAttribute> getMatAttributes() throws IOException {
        MatAttributesEntry data = objectMapper.readValue(new URL(matAttributesUrl), MatAttributesEntry.class);

        if (data != null && data.getEntry() != null) {
            log.info(LOG_MESSAGE, data.getEntry().size(), matAttributesUrl);
            return data.getEntry().stream().map(e -> {
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
        QdmToQicoreMappingEntry data = objectMapper.readValue(new URL(qdmQiCoreMappingUrl), QdmToQicoreMappingEntry.class);

        if (data != null && data.getEntry() != null) {
            log.info(LOG_MESSAGE, data.getEntry().size(), qdmQiCoreMappingUrl);
            return data.getEntry().stream().map(e -> {
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
        DataTypesEntry data = objectMapper.readValue(new URL(dataTypesUrl), DataTypesEntry.class);

        if (data != null && data.getEntry() != null) {
            log.info(LOG_MESSAGE, data.getEntry().size(), dataTypesUrl);
            return data.getEntry().stream().map(e -> {
                var d = new DataType();
                d.setDataType(e.getDataType());
                d.setRegex(e.getRegex());
                d.setType(e.getFieldType());
                d.setValidValues(e.getValidValues());
                return d;
            }).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    @Cacheable("requiredMeasureFields")
    public List<RequiredMeasureField> getRequiredMeasureFields() throws IOException {
        RequiredFieldsEntry data = objectMapper.readValue(new URL(requiredMeasureFieldsUrl), RequiredFieldsEntry.class);

        if (data != null && data.getEntry() != null) {
            log.info(LOG_MESSAGE, data.getEntry().size(), requiredMeasureFieldsUrl);
            return data.getEntry().stream().map(e -> {
                var r = new RequiredMeasureField();
                r.setField(e.getField());
                r.setType(e.getFieldType());
                return r;
            }).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    @Cacheable("resourceDefinitions")
    public List<ResourceDefinition> getResourceDefinitions() throws IOException {
        ResourceDefinitionEntry data = objectMapper.readValue(new URL(resourceDefinitionUrl), ResourceDefinitionEntry.class);

        if (data != null && data.getEntry() != null) {
            log.info(LOG_MESSAGE, data.getEntry().size(), resourceDefinitionUrl);
            return data.getEntry().stream().map(e -> {
                var r = new ResourceDefinition();
                r.setCardinality(getData(e.getCardinality()));
                r.setDefinition(getData(e.getDefinition()));
                r.setElementId(getData(e.getElementId()));
                r.setIsModifier(getData(e.getIsModifier()));
                r.setIsSummary(getData(e.getSummary()));
                r.setType(getData(e.getType()));
                return r;
            }).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    @Cacheable("conversionDataTypes")
    public List<ConversionDataTypes> getConversionDataTypes() throws IOException {
        ConversionDataTypesEntry data = objectMapper.readValue(new URL(conversionDataTypesUrl), ConversionDataTypesEntry.class);

        if (data != null && data.getEntry() != null) {
            log.info(LOG_MESSAGE, data.getEntry().size(), conversionDataTypesUrl);
            return data.getEntry().stream().map(e -> {
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
        ConversionAttributesEntry data = objectMapper.readValue(new URL(attributesUrl), ConversionAttributesEntry.class);

        if (data != null && data.getEntry() != null) {
            log.info(LOG_MESSAGE, data.getEntry().size(), attributesUrl);
            return data.getEntry().stream().map(e -> {
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
        FhirLightBoxDatatypeAttributeAssociationEntry data = objectMapper.readValue(new URL(fhirLightboxDatatypeAttributeAssociationUrl), FhirLightBoxDatatypeAttributeAssociationEntry.class);

        if (data != null && data.getEntry() != null) {
            log.info(LOG_MESSAGE, data.getEntry().size(), fhirLightboxDatatypeAttributeAssociationUrl);
            return data.getEntry().stream().map(e -> {
                var r = new FhirLightBoxDatatypeAttributeAssociations();
                r.setDatatype(e.getDatatype());
                r.setAttribute(e.getAttribute());
                r.setAttributeType(e.getAttributeType());

                String hasBinding = e.getHasBinding();

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
        FhirLightBoxDataTypesForFunctionArgsEntry data = objectMapper.readValue(new URL(fhirLightboxDataTypesForFunctionArgsUrl), FhirLightBoxDataTypesForFunctionArgsEntry.class);

        if (data != null && !CollectionUtils.isEmpty(data.getEntry())) {
            log.info(LOG_MESSAGE, data.getEntry().size(), fhirLightboxDataTypesForFunctionArgsUrl);

            return data.getEntry().stream()
                    .sorted(String::compareToIgnoreCase)
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    @Cacheable("populationBasisValidValues")
    public Collection<String> getPopulationBasisValidValues() throws IOException {
        PopulationBasisValidValues data = objectMapper.readValue(new URL(populationBasisValidValuesUrl), PopulationBasisValidValues.class);

        if (data != null && !CollectionUtils.isEmpty(data.getEntry())) {
            log.info(LOG_MESSAGE, data.getEntry().size(), populationBasisValidValuesUrl);

            return data.getEntry().stream()
                    .sorted()
                    .collect(Collectors.toCollection(LinkedHashSet::new));
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
