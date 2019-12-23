package gov.cms.mat.fhir.services.service;

import gov.cms.mat.fhir.rest.cql.ConversionMapping;
import gov.cms.mat.fhir.services.exceptions.QdmQiCoreDataException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QdmQiCoreDataServiceImplTest {
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private QdmQiCoreDataService qdmQiCoreDataService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(qdmQiCoreDataService, "baseURL", "http://howdy.doody.com");
    }

    @Test
    void findByFhirR4QiCoreMapping_Success() {
        ConversionMapping conversionToReturn = ConversionMapping.builder().build();
        when(restTemplate.getForObject(any(), any())).thenReturn(conversionToReturn);

        ConversionMapping conversionReturned =
                qdmQiCoreDataService.findByFhirR4QiCoreMapping("ValueSet.bar", "core");

        assertEquals(conversionToReturn, conversionReturned);

        verify(restTemplate).getForObject(any(), any());
    }

    @Test
    void findByFhirR4QiCoreMapping_BadMatObjectWithAttribute() {
        String badMatObjectWithAttribute = "mat";

        QdmQiCoreDataException thrown =
                Assertions.assertThrows(QdmQiCoreDataException.class, () -> {
                    qdmQiCoreDataService.findByFhirR4QiCoreMapping(badMatObjectWithAttribute, "core");
                });

        assertTrue(thrown.getMessage().contains(badMatObjectWithAttribute));

        verifyNoInteractions(restTemplate);
    }

    @Test
    void findByFhirR4QiCoreMapping_RestTemplateThrows() {
        String errorMessage = "mat error!!";

        when(restTemplate.getForObject(any(), any())).thenThrow(new RestClientException(errorMessage));

        QdmQiCoreDataException thrown =
                Assertions.assertThrows(QdmQiCoreDataException.class, () -> {
                    qdmQiCoreDataService.findByFhirR4QiCoreMapping("Mat.foo", "core");
                });

        assertTrue(thrown.getMessage().contains(errorMessage));

        verify(restTemplate).getForObject(any(), any());
    }
}