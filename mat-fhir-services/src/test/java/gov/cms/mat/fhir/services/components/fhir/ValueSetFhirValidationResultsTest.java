package gov.cms.mat.fhir.services.components.fhir;

import gov.cms.mat.fhir.services.components.mongo.ConversionResultsService;
import gov.cms.mat.fhir.services.components.mongo.ThreadSessionKey;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.exceptions.ValueSetValidationException;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;

import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ValueSetFhirValidationResultsTest {
    private static final String MEASURE_ID = "measure_id";

    private ThreadSessionKey threadSessionKey;

    @Mock
    private HapiFhirServer hapiFhirServer;
    @Mock
    private ConversionResultsService conversionResultsService;

    @InjectMocks
    private ValueSetFhirValidationResults valueSetFhirValidationResults;

    @BeforeEach
    public void setUp() {
        threadSessionKey = ThreadSessionKey.builder()
                .measureId(MEASURE_ID)
                .start(Instant.now())
                .build();
    }

    @Test
    void generate_NoValueSets() {
        Assertions.assertThrows(ValueSetValidationException.class, () -> {
            valueSetFhirValidationResults.generate(Collections.emptyList(), XmlSource.SIMPLE, MEASURE_ID);
        });

        verifyNoInteractions(hapiFhirServer);
    }

}