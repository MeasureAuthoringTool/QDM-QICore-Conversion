package gov.cms.mat.qdmqicore.conversion.service;

import gov.cms.mat.qdmqicore.conversion.data.SearchData;
import gov.cms.mat.qdmqicore.conversion.dto.ConversionDataBuilder;
import gov.cms.mat.fhir.rest.dto.ConversionMapping;
import gov.cms.mat.qdmqicore.conversion.spread_sheet_data.ConversionEntry;
import gov.cms.mat.qdmqicore.conversion.spread_sheet_data.FhirQdmMappingData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversionDataServiceTest implements ConversionDataBuilder {
    @Mock
    private FhirQdmMappingData fhirQdmMappingData;
    @InjectMocks
    private ConversionDataService conversionDataService;

    private ConversionMapping conversionMapping;

    @BeforeEach
    void setUp() {
        conversionMapping = buildConversionMapping();
        ConversionEntry conversionEntry = createConversionEntryWithData(conversionMapping);

        when(fhirQdmMappingData.getAll()).thenReturn(Collections.singletonList(conversionEntry));
    }


    @Test
    void getAll() {
        List<ConversionMapping> mappingList = conversionDataService.getAll();
        assertEquals(1, mappingList.size());


        verify(fhirQdmMappingData).getAll();
    }

    @Test
    void find_Success() {
        SearchData searchData = SearchData.builder().build();

        List<ConversionMapping> mappingList = conversionDataService.find(searchData);
        assertEquals(1, mappingList.size());

        verify(fhirQdmMappingData).getAll();
    }

    @Test
    void find_NotFound() {
        SearchData searchData = SearchData.builder()
                .fhirResource("___Not_Found___")
                .build();

        List<ConversionMapping> mappingList = conversionDataService.find(searchData);
        assertTrue(mappingList.isEmpty());

        verify(fhirQdmMappingData).getAll();
    }


}