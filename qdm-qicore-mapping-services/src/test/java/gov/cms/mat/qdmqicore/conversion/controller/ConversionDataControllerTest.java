package gov.cms.mat.qdmqicore.conversion.controller;

import gov.cms.mat.fhir.rest.dto.ConversionMapping;
import gov.cms.mat.qdmqicore.conversion.exceptions.ConversionMappingDataError;
import gov.cms.mat.qdmqicore.conversion.exceptions.ConversionMappingNotFound;
import gov.cms.mat.qdmqicore.conversion.service.ConversionDataService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversionDataControllerTest {
    @Mock
    private ConversionDataService conversionDataService;
    @InjectMocks
    private ConversionDataController conversionDataController;

    @Test
    void getAll() {
        List<ConversionMapping> listToReturn = Collections.singletonList(ConversionMapping.builder().build());
        when(conversionDataService.getAll()).thenReturn(listToReturn);

        assertEquals(listToReturn, conversionDataController.getAll());

        verify(conversionDataService).getAll();
    }

    @Test
    void find() {
        List<ConversionMapping> listToReturn = Collections.singletonList(ConversionMapping.builder().build());
        when(conversionDataService
                .find(any())).thenReturn(listToReturn);

        assertEquals(listToReturn, conversionDataController.find(null, null,
                null, null, null, null));

        verify(conversionDataService).find(any());
    }

    @Test
    void findOneSuccess() {
        ConversionMapping conversionMappingToReturn = ConversionMapping.builder().build();
        List<ConversionMapping> listToReturn = Collections.singletonList(conversionMappingToReturn);
        when(conversionDataService
                .find(any())).thenReturn(listToReturn);

        ConversionMapping conversionMappingReturned =
                conversionDataController.findOne("name_ok", "description_ok");

        assertEquals(conversionMappingToReturn, conversionMappingReturned);

        verify(conversionDataService).find(any());
    }

    @Test
    void findOneNotFound() {
        when(conversionDataService
                .find(any())).thenReturn(Collections.emptyList());

        Assertions.assertThrows(ConversionMappingNotFound.class,
                () -> conversionDataController.findOne("name", "description"));

        verify(conversionDataService).find(any());
    }

    @Test
    void findOneTooManyFound() {
        List<ConversionMapping> listToReturn =
                Arrays.asList(ConversionMapping.builder().build(), ConversionMapping.builder().build());

        when(conversionDataService
                .find(any())).thenReturn(listToReturn);

        Assertions.assertThrows(ConversionMappingDataError.class,
                () -> conversionDataController.findOne("mat_name", "mat_description"));

        verify(conversionDataService).find(any());
    }
}