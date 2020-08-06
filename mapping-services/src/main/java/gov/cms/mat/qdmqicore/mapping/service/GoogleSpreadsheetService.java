package gov.cms.mat.qdmqicore.mapping.service;

import gov.cms.mat.fhir.rest.dto.spreadsheet.ConversionAttributes;
import gov.cms.mat.fhir.rest.dto.spreadsheet.ConversionDataTypes;
import gov.cms.mat.fhir.rest.dto.spreadsheet.DataType;
import gov.cms.mat.fhir.rest.dto.spreadsheet.MatAttribute;
import gov.cms.mat.fhir.rest.dto.spreadsheet.QdmToQicoreMapping;
import gov.cms.mat.fhir.rest.dto.spreadsheet.RequiredMeasureField;
import gov.cms.mat.fhir.rest.dto.spreadsheet.ResourceDefinition;
import gov.cms.mat.qdmqicore.mapping.model.google.GoogleConversionAttributesData;
import gov.cms.mat.qdmqicore.mapping.model.google.GoogleConversionDataTypesData;
import gov.cms.mat.qdmqicore.mapping.model.google.GoogleDataTypesData;
import gov.cms.mat.qdmqicore.mapping.model.google.GoogleMatAttributesData;
import gov.cms.mat.qdmqicore.mapping.model.google.GoogleQdmToQicoreMappingData;
import gov.cms.mat.qdmqicore.mapping.model.google.GoogleRequiredFieldsData;
import gov.cms.mat.qdmqicore.mapping.model.google.GoogleResourceDefinitionData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
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

    public GoogleSpreadsheetService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Cacheable("matAttributes")
    public List<MatAttribute> getMatAttributes() {
        GoogleMatAttributesData data = restTemplate.getForObject(matAttributesUrl, GoogleMatAttributesData.class);

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
    public List<QdmToQicoreMapping> getQdmToQicoreMapping() {
        GoogleQdmToQicoreMappingData data = restTemplate.getForObject(qdmQiCoreMappingUrl, GoogleQdmToQicoreMappingData.class);

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
    public List<DataType> getDataTypes() {
        GoogleDataTypesData data = restTemplate.getForObject(dataTypesUrl, GoogleDataTypesData.class);

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
    public List<RequiredMeasureField> getRequiredMeasureFields() {
        GoogleRequiredFieldsData data = restTemplate.getForObject(requiredMeasureFieldsUrl, GoogleRequiredFieldsData.class);

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
    public List<ResourceDefinition> getResourceDefinitions() {
        GoogleResourceDefinitionData data = restTemplate.getForObject(resourceDefinitionUrl, GoogleResourceDefinitionData.class);

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
    public List<ConversionDataTypes> getConversionDataTypes() {
        GoogleConversionDataTypesData data = restTemplate.getForObject(conversionDataTypesUrl, GoogleConversionDataTypesData.class);

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
    public List<ConversionAttributes> getConversionAttributes() {
        GoogleConversionAttributesData data = restTemplate.getForObject(attributesUrl, GoogleConversionAttributesData.class);

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
}
