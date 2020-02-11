package gov.cms.mat.qdmqicore.conversion.service;

import gov.cms.mat.fhir.rest.dto.ConversionMapping;
import gov.cms.mat.qdmqicore.conversion.config.CqlConfigProperties;
import gov.cms.mat.qdmqicore.conversion.data.SearchData;
import gov.cms.mat.qdmqicore.conversion.spread_sheet_data.ConversionEntry;
import gov.cms.mat.qdmqicore.conversion.spread_sheet_data.FhirQdmMappingData;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConversionDataService {
    private final FhirQdmMappingData fhirQdmMappingData;

    private final CqlConfigProperties cqlConfigProperties;

    public ConversionDataService(FhirQdmMappingData fhirQdmMappingData,
                                 CqlConfigProperties cqlConfigProperties) {
        this.fhirQdmMappingData = fhirQdmMappingData;
        this.cqlConfigProperties = cqlConfigProperties;
    }

    public List<ConversionMapping> getAll() {
        return fhirQdmMappingData.getAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<ConversionMapping> find(SearchData searchData) {
        return fhirQdmMappingData.getAll()
                .stream()
                .filter(conversionEntry -> filterBySearchData(conversionEntry, searchData))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<ConversionMapping> filtered(SearchData searchData) {
        return fhirQdmMappingData.getAll()
                .stream()
                .filter(this::filterNulls)
                .filter(s -> filterNegations(s))
                .filter(conversionEntry -> filterBySearchData(conversionEntry, searchData))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private boolean filterNegations(ConversionEntry cm) {
        return StringUtils.indexOfAny(cm.getMatDataTypeDescriptionData(),
                cqlConfigProperties.getNegations().toArray(new String[0])) < 0;
    }

    private boolean filterNulls(ConversionEntry cm) {
        return (StringUtils.isNotEmpty(cm.getFhirResourceData()) && StringUtils.isNotEmpty(cm.getFhirElementData()));

    }


    private ConversionMapping mapToDto(ConversionEntry entry) {
        return ConversionMapping.builder()
                .title(entry.getTitleData())
                .matDataTypeDescription(entry.getMatDataTypeDescriptionData())
                .matAttributeName(entry.getMatAttributeNameData())
                .fhirR4QiCoreMapping(entry.getFhirR4QiCoreMappingData())
                .fhirResource(entry.getFhirResourceData())
                .fhirElement(entry.getFhirElementData())
                .fhirType(entry.getFhirTypeData())
                .build();
    }

    private boolean filterBySearchData(ConversionEntry conversionEntry, SearchData searchData) {
        return isFiltered(conversionEntry.getFhirResourceData(), searchData.getFhirResource())
                && isFiltered(conversionEntry.getMatAttributeNameData(), searchData.getMatAttributeName())
                && isFiltered(conversionEntry.getFhirR4QiCoreMappingData(), searchData.getFhirR4QiCoreMapping())
                && isFiltered(conversionEntry.getFhirElementData(), searchData.getFhirElement())
                && isFiltered(conversionEntry.getFhirTypeData(), searchData.getFhirType())
                && isFiltered(conversionEntry.getMatDataTypeDescriptionData(), searchData.getMatDataTypeDescription());
    }

    private boolean isFiltered(String conversionEntryString, String searchDataString) {
        if (StringUtils.isEmpty(searchDataString)) {
            return true;
        }

        return searchDataString.equals(conversionEntryString);
    }
}
