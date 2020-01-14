package gov.cms.mat.fhir.services.service.orchestration;

import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.services.components.fhir.MeasureGroupingDataProcessor;
import gov.cms.mat.fhir.services.components.fhir.RiskAdjustmentsDataProcessor;
import gov.cms.mat.fhir.services.components.fhir.SupplementalDataProcessor;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.translate.ManageMeasureDetailMapper;
import gov.cms.mat.fhir.services.translate.MeasureTranslator;
import lombok.extern.slf4j.Slf4j;
import mat.client.measure.ManageCompositeMeasureDetailModel;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FhirMeasureCreator {
    private final ManageMeasureDetailMapper manageMeasureDetailMapper;
    private final HapiFhirServer hapiFhirServer;
    private final SupplementalDataProcessor supplementalDataProcessor;
    private final RiskAdjustmentsDataProcessor riskAdjustmentsDataProcessor;
    private final MeasureGroupingDataProcessor measureGroupingDataProcessor;

    public FhirMeasureCreator(ManageMeasureDetailMapper manageMeasureDetailMapper,
                              HapiFhirServer hapiFhirServer,
                              SupplementalDataProcessor supplementalDataProcessor,
                              RiskAdjustmentsDataProcessor riskAdjustmentsDataProcessor,
                              MeasureGroupingDataProcessor measureGroupingDataProcessor) {
        this.manageMeasureDetailMapper = manageMeasureDetailMapper;
        this.hapiFhirServer = hapiFhirServer;
        this.supplementalDataProcessor = supplementalDataProcessor;
        this.riskAdjustmentsDataProcessor = riskAdjustmentsDataProcessor;
        this.measureGroupingDataProcessor = measureGroupingDataProcessor;
    }

    public org.hl7.fhir.r4.model.Measure create(Measure matMeasure, byte[] xmlBytes, String narrative) {
        ManageCompositeMeasureDetailModel model = manageMeasureDetailMapper.convert(xmlBytes, matMeasure);

        MeasureTranslator fhirMapper = new MeasureTranslator(model, narrative, hapiFhirServer.getBaseURL());
        org.hl7.fhir.r4.model.Measure fhirMeasure = fhirMapper.translateToFhir();

        if (ArrayUtils.isNotEmpty(xmlBytes)) {
            String xml = new String(xmlBytes);

            fhirMeasure.setSupplementalData(supplementalDataProcessor.processXml(xml));
            fhirMeasure.setRiskAdjustment(riskAdjustmentsDataProcessor.processXml(xml));

            fhirMeasure.setGroup(measureGroupingDataProcessor.processXml(xml));
        }
        return fhirMeasure;
    }
}
