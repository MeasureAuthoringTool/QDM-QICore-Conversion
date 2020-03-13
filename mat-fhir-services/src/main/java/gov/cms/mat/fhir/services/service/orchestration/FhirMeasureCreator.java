package gov.cms.mat.fhir.services.service.orchestration;

import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.services.components.fhir.MeasureGroupingDataProcessor;
import gov.cms.mat.fhir.services.components.fhir.RiskAdjustmentsDataProcessor;
import gov.cms.mat.fhir.services.components.fhir.SupplementalDataProcessor;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.translate.ManageMeasureDetailMapper;
import gov.cms.mat.fhir.services.translate.MeasureTranslator;
import lombok.extern.slf4j.Slf4j;
import mat.client.measure.ManageCompositeMeasureDetailModel;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;

import static gov.cms.mat.fhir.rest.dto.ConversionOutcome.INVALID_MEASURE_XML;

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

    public ManageCompositeMeasureDetailModel buildModel(byte[] xmlBytes,Measure matMeasure) {
        try {
            return manageMeasureDetailMapper.convert(xmlBytes, matMeasure);
        } catch (RuntimeException e) {
            ConversionReporter.setTerminalMessage(e.getMessage(),INVALID_MEASURE_XML);
            throw e;
        }
    }

    public org.hl7.fhir.r4.model.Measure create(Measure matMeasure, byte[] xmlBytes, String narrative) {
        ManageCompositeMeasureDetailModel model = buildModel(xmlBytes,matMeasure);

        MeasureTranslator fhirMapper = new MeasureTranslator(matMeasure,model,narrative, hapiFhirServer.getBaseURL());
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
