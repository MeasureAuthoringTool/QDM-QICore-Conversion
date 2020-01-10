package gov.cms.mat.fhir.services.components.fhir;

import gov.cms.mat.fhir.services.components.mat.MatXmlConverter;
import lombok.extern.slf4j.Slf4j;
import mat.model.RiskAdjustmentDTO;
import mat.model.cql.CQLDefinitionsWrapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class RiskAdjustmentsDataProcessor {
    private final MatXmlConverter matXmlConverter;

    public RiskAdjustmentsDataProcessor(MatXmlConverter matXmlConverter) {
        this.matXmlConverter = matXmlConverter;
    }

    public String processXml(String xml) {
        CQLDefinitionsWrapper cqlDefinitionsWrapper = matXmlConverter.toCQLDefinitionsRiskAdjustments(xml);

        if (CollectionUtils.isEmpty(cqlDefinitionsWrapper.getRiskAdjVarDTOList())) {
            return null;
        } else {
            return processWrapper(cqlDefinitionsWrapper.getRiskAdjVarDTOList());
        }
    }

    private String processWrapper(List<RiskAdjustmentDTO> riskAdjustments) {
        if (riskAdjustments.size() > 1) {
            log.warn("RiskAdjustmentDTO list greater than 1, list: {}", riskAdjustments);
        }

        return riskAdjustments.get(0).getName();
    }
}
