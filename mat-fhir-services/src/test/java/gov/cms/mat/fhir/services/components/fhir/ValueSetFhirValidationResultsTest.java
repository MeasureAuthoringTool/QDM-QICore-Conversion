package gov.cms.mat.fhir.services.components.fhir;

import gov.cms.mat.fhir.rest.dto.ConversionType;
import gov.cms.mat.fhir.rest.dto.ValueSetConversionResults;
import gov.cms.mat.fhir.services.components.reporting.ConversionReporter;
import gov.cms.mat.fhir.services.components.reporting.ConversionResult;
import gov.cms.mat.fhir.services.components.reporting.ConversionResultsService;
import gov.cms.mat.fhir.services.components.reporting.ThreadSessionKey;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.exceptions.ValueSetValidationException;
import gov.cms.mat.fhir.services.summary.FhirValueSetResourceValidationResult;
import org.hl7.fhir.r4.model.ValueSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValueSetFhirValidationResultsTest {
    private static final String MEASURE_ID = "measure_id";

    @Mock
    private ConversionResultsService conversionResultsService;
    @InjectMocks
    private ValueSetFhirValidationResults valueSetFhirValidationResults;

    @Test
    void generate_NoValueSets() {
        Assertions.assertThrows(ValueSetValidationException.class, () -> {
            valueSetFhirValidationResults.generate(Collections.emptyList(), XmlSource.SIMPLE, MEASURE_ID);
        });
    }

    @Test
    void generate() {

        ThreadSessionKey key = setUpReporter();
        ValueSetConversionResults valueSetConversionResults = new ValueSetConversionResults();

        ConversionResult conversionResult = new ConversionResult();
        conversionResult.getValueSetConversionResults().add(valueSetConversionResults);
        when(conversionResultsService.findConversionResult(key)).thenReturn(conversionResult);

        FhirValueSetResourceValidationResult result =
                valueSetFhirValidationResults.generate(List.of(new ValueSet()), XmlSource.SIMPLE, MEASURE_ID);


        assertEquals(XmlSource.SIMPLE, result.getXmlSource() );
        assertEquals(1, result.getValueSetConversionResults().size());
        assertEquals( valueSetConversionResults, result.getValueSetConversionResults().get(0));
    }

    private ThreadSessionKey setUpReporter() {
        return ConversionReporter.setInThreadLocal(MEASURE_ID,
                "TEST",
                conversionResultsService,
                Instant.now(),
                ConversionType.CONVERSION,
                XmlSource.SIMPLE,
                Boolean.TRUE,
                null);
    }

}