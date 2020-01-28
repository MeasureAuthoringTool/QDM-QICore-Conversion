package gov.cms.mat.qdmqicore.conversion.service;

import gov.cms.mat.qdmqicore.conversion.data.SearchData;
import gov.cms.mat.qdmqicore.conversion.dto.ConversionMapping;
import gov.cms.mat.qdmqicore.conversion.spread_sheet_data.ConversionEntry;
import gov.cms.mat.qdmqicore.conversion.spread_sheet_data.FhirQdmMappingData;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConversionDataService {
    private final FhirQdmMappingData fhirQdmMappingData;

    public ConversionDataService(FhirQdmMappingData fhirQdmMappingData) {
        this.fhirQdmMappingData = fhirQdmMappingData;
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
                .filter(conversionEntry -> filter(conversionEntry, searchData))
                .map(this::mapToDto)
                .collect(Collectors.toList());
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

    private boolean filter(ConversionEntry conversionEntry, SearchData searchData) {
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
