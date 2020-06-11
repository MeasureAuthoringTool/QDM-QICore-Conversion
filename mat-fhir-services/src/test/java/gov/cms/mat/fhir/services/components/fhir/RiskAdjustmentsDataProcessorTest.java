package gov.cms.mat.fhir.services.components.fhir;

import gov.cms.mat.fhir.services.components.mat.MatXmlConverter;
import gov.cms.mat.fhir.services.translate.processor.RiskAdjustmentsDataProcessor;
import mat.model.RiskAdjustmentDTO;
import mat.model.cql.CQLDefinitionsWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiskAdjustmentsDataProcessorTest {
    private static final String XML = "<xml>xml</xml>";
    private static final String NAME = "name";

    @Mock
    private MatXmlConverter matXmlConverter;

    @InjectMocks
    private RiskAdjustmentsDataProcessor riskAdjustmentsDataProcessor;

    @Test
    void processXml_RiskAdsMT() {
        when(matXmlConverter.toCQLDefinitionsRiskAdjustments(XML)).thenReturn(new CQLDefinitionsWrapper());

        assertNull(riskAdjustmentsDataProcessor.processXml(XML));

        verify(matXmlConverter).toCQLDefinitionsRiskAdjustments(XML);
    }

    @Test
    void processXml_HappyPath() {
        CQLDefinitionsWrapper cqlDefinitionsWrapper = getCqlDefinitionsWrapper();

        when(matXmlConverter.toCQLDefinitionsRiskAdjustments(XML)).thenReturn(cqlDefinitionsWrapper);

        assertEquals(NAME, riskAdjustmentsDataProcessor.processXml(XML));

        verify(matXmlConverter).toCQLDefinitionsRiskAdjustments(XML);
    }

    @Test /* When more than one will return the first one */
    void processXml_MoreThanOneAdjustment() {
        CQLDefinitionsWrapper cqlDefinitionsWrapper = getCqlDefinitionsWrapper();

        RiskAdjustmentDTO riskAdjustmentDTO = new RiskAdjustmentDTO();
        riskAdjustmentDTO.setName("not_my" + NAME);
        cqlDefinitionsWrapper.getRiskAdjVarDTOList().add(riskAdjustmentDTO);

        when(matXmlConverter.toCQLDefinitionsRiskAdjustments(XML)).thenReturn(cqlDefinitionsWrapper);

        assertEquals(NAME, riskAdjustmentsDataProcessor.processXml(XML));

        verify(matXmlConverter).toCQLDefinitionsRiskAdjustments(XML);
    }

    private CQLDefinitionsWrapper getCqlDefinitionsWrapper() {
        CQLDefinitionsWrapper cqlDefinitionsWrapper = new CQLDefinitionsWrapper();
        RiskAdjustmentDTO riskAdjustmentDTO = new RiskAdjustmentDTO();
        riskAdjustmentDTO.setName(NAME);
        List<RiskAdjustmentDTO> list = new ArrayList<>();
        list.add(riskAdjustmentDTO);
        cqlDefinitionsWrapper.setRiskAdjVarDTOList(list);
        return cqlDefinitionsWrapper;
    }
}