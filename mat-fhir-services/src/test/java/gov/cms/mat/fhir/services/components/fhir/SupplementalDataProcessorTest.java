package gov.cms.mat.fhir.services.components.fhir;

import gov.cms.mat.fhir.services.components.mat.MatXmlConverter;
import mat.model.cql.CQLDefinition;
import mat.model.cql.CQLDefinitionsWrapper;
import org.hl7.fhir.r4.model.Measure;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupplementalDataProcessorTest {
    private static final String XML = "<xml>xml</xml>";
    private static final String NAME = "name";

    @Mock
    private MatXmlConverter matXmlConverter;

    @InjectMocks
    private SupplementalDataProcessor supplementalDataProcessor;

    @Test
    void processXml_NoSupplementalDataFound() {
        when(matXmlConverter.toCQLDefinitionsSupplementalData(XML)).thenReturn(new CQLDefinitionsWrapper());

        assertTrue(supplementalDataProcessor.processXml(XML).isEmpty());
    }

    @Test
    void processXml_DataFound() {
        CQLDefinitionsWrapper cqlDefinitionsWrapper = new CQLDefinitionsWrapper();

        CQLDefinition cqlDefinition = new CQLDefinition();
        cqlDefinition.setName(NAME);

        cqlDefinitionsWrapper.setCqlDefinitions(Collections.singletonList(cqlDefinition));

        when(matXmlConverter.toCQLDefinitionsSupplementalData(XML)).thenReturn(cqlDefinitionsWrapper);

        List<Measure.MeasureSupplementalDataComponent> list = supplementalDataProcessor.processXml(XML);
        assertEquals(1, list.size());

        assertEquals(NAME, list.get(0).getCriteria().getExpression());
    }
}