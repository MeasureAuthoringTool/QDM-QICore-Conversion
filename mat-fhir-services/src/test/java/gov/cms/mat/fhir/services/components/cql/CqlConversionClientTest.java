package gov.cms.mat.fhir.services.components.cql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CqlConversionClientTest {
    @Mock
    private RestTemplate restTemplate;
    @InjectMocks
    private CqlConversionClient cqlConversionClient;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(cqlConversionClient, "baseURL", "http://acme.com");
    }

    @Test
    void getJson() {
        String json = "json";
        when(restTemplate.exchange(any(RequestEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(json, HttpStatus.ACCEPTED));

        ResponseEntity<String> entity = cqlConversionClient.getJson("CQL");
        assertEquals(json, entity.getBody());
        verify(restTemplate).exchange(any(RequestEntity.class), eq(String.class));
    }

    @Test
    void getCql() {
    }
}