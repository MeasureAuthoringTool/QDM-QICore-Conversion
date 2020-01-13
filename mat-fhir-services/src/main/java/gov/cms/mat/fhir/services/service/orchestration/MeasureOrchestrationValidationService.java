package gov.cms.mat.fhir.services.service.orchestration;


import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.commons.model.MeasureExport;
import gov.cms.mat.fhir.rest.dto.FhirValidationResult;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.components.xml.MatXmlProcessor;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.rest.support.FhirValidatorProcessor;
import gov.cms.mat.fhir.services.service.MeasureExportDataService;
import gov.cms.mat.fhir.services.summary.FhirMeasureResourceValidationResult;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class MeasureOrchestrationValidationService implements FhirValidatorProcessor {

    private final MeasureExportDataService measureExportDataService;
    private final MatXmlProcessor matXmlProcessor;
    private final HapiFhirServer hapiFhirServer;


    private final FhirMeasureCreator fhirMeasureCreator;

    public MeasureOrchestrationValidationService(MeasureExportDataService measureExportDataService,
                                                 MatXmlProcessor matXmlProcessor,
                                                 HapiFhirServer hapiFhirServer, FhirMeasureCreator fhirMeasureCreator) {
        this.measureExportDataService = measureExportDataService;
        this.matXmlProcessor = matXmlProcessor;
        this.hapiFhirServer = hapiFhirServer;

        this.fhirMeasureCreator = fhirMeasureCreator;
    }

    boolean validate(OrchestrationProperties properties) {
        org.hl7.fhir.r4.model.Measure fhirMeasure = processFhirMeasure(properties);

        FhirMeasureResourceValidationResult response =
                new FhirMeasureResourceValidationResult(properties.getMeasureId(), "Measure");

        validateResource(response, fhirMeasure, hapiFhirServer.getCtx());
        List<FhirValidationResult> list = buildResults(response);

        ConversionReporter.setFhirMeasureValidationResults(list);

        return true; //todo most will fail miserably let pass for now todo look for weeoe MeasureConversionResults
    }


    private org.hl7.fhir.r4.model.Measure processFhirMeasure(OrchestrationProperties properties) {
        org.hl7.fhir.r4.model.Measure fhirMeasure = buildFhirMeasure(properties);

        properties.setFhirMeasure(fhirMeasure);
        ConversionReporter.setFhirMeasureJson(hapiFhirServer.toJson(fhirMeasure));

        return fhirMeasure;

    }


    private org.hl7.fhir.r4.model.Measure buildFhirMeasure(OrchestrationProperties properties) {
        byte[] xmlBytes = matXmlProcessor.getXml(properties.getMatMeasure(), properties.getXmlSource());

        MeasureExport measureExport = measureExportDataService.findByIdRequired(properties.getMeasureId());

        //human-readable may exist not an error if it doesn't
        String narrative = getNarrative(measureExport);

        return createFhirMeasure(properties.getMatMeasure(), xmlBytes, narrative);
    }


    public String getNarrative(MeasureExport measureExport) {
        try {
            return new String(measureExport.getHumanReadable());
        } catch (Exception ex) {
            log.error("Narrative not found: {}", ex.getMessage());
            return "";
        }
    }

    public org.hl7.fhir.r4.model.Measure createFhirMeasure(Measure matMeasure, byte[] xmlBytes, String narrative) {
        return fhirMeasureCreator.create(matMeasure, xmlBytes, narrative);
    }
}
