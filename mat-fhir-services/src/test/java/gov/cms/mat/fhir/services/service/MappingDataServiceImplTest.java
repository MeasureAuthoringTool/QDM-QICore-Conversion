package gov.cms.mat.fhir.services.service;

import gov.cms.mat.fhir.rest.dto.spreadsheet.QdmToFhirMappingHelper;
import gov.cms.mat.fhir.rest.dto.spreadsheet.QdmToQicoreMapping;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MappingDataServiceImplTest {
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private MappingDataService mappingDataService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mappingDataService, "matAtttributesUrl", "http://howdy.doody.com");
        ReflectionTestUtils.setField(mappingDataService, "self", mappingDataService);
    }

    @Test
    void findByFhirR4QiCoreMapping_Success() {
        var attrs = new QdmToQicoreMapping[1];
        attrs[0] = new QdmToQicoreMapping();
        attrs[0].setMatAttributeType("bar");
        attrs[0].setMatDataType("ValueSet");
        attrs[0].setFhirQICoreMapping("FhirValueSet.fhirBar");
        when(restTemplate.getForObject("http://howdy.doody.com", QdmToQicoreMapping[].class)).thenReturn(attrs);

        QdmToFhirMappingHelper helper = mappingDataService.getQdmToFhirMappingHelper();

        assertEquals("FhirValueSet", helper.convertType("ValueSet"));
        assertEquals("FhirValueSet.fhirBar", helper.convertTypeAndAttribute("ValueSet","bar"));
        assertEquals("Nada", helper.convertType("Nada"));
        assertEquals("Nada.indude", helper.convertTypeAndAttribute("Nada","indude"));
        assertEquals("FhirValueSet.indude", helper.convertTypeAndAttribute("ValueSet","indude"));

        verify(restTemplate).getForObject("http://howdy.doody.com", QdmToQicoreMapping[].class);
    }
}