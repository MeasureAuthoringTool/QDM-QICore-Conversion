package gov.cms.mat.fhir.services.service.orchestration;

import gov.cms.mat.cql.dto.CqlConversionPayload;
import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.exceptions.HapiResourceValidationException;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.repository.MeasureExportRepository;
import gov.cms.mat.fhir.services.service.CQLLibraryTranslationService;
import gov.cms.mat.fhir.services.service.CqlLibraryDataService;
import gov.cms.mat.fhir.services.service.MeasureDataService;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import gov.cms.mat.fhir.services.translate.LibraryTranslator;
import gov.cms.mat.fhir.services.translate.creators.FhirCreator;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Narrative;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Slf4j
public class PushMeasureService implements FhirCreator {
    private final MeasureOrchestrationValidationService measureOrchestrationValidationService;
    private final MeasureOrchestrationConversionService measureOrchestrationConversionService;
    private final OrchestrationService orchestrationService;
    private final MeasureDataService measureDataService;
    private final CqlLibraryDataService cqlLibraryDataService;
    private final HapiFhirServer hapiFhirServer;
    private final CQLLibraryTranslationService cqlLibraryTranslationService;
    private final LibraryTranslator libTranslator;

    public PushMeasureService(MeasureOrchestrationValidationService measureOrchestrationValidationService,
                              MeasureOrchestrationConversionService measureOrchestrationConversionService,
                              OrchestrationService orchestrationService,
                              MeasureDataService measureDataService,
                              CqlLibraryDataService cqlLibraryDataService,
                              HapiFhirServer hapiFhirServer,
                              CQLLibraryTranslationService cqlLibraryTranslationService,
                              MeasureExportRepository measureExportRepo,
                              LibraryTranslator libTranslator) {
        this.measureOrchestrationValidationService = measureOrchestrationValidationService;
        this.measureOrchestrationConversionService = measureOrchestrationConversionService;
        this.orchestrationService = orchestrationService;
        this.measureDataService = measureDataService;
        this.cqlLibraryDataService = cqlLibraryDataService;
        this.hapiFhirServer = hapiFhirServer;
        this.cqlLibraryTranslationService = cqlLibraryTranslationService;
        this.libTranslator = libTranslator;
    }

    public String convert(String id, OrchestrationProperties orchestrationProperties) {
        Measure measure = measureDataService.findOneValid(id);
        orchestrationProperties.setMatMeasure(measure);

        CqlLibrary cqlLib = cqlLibraryDataService.getMeasureLib(measure.getId());

        if (cqlLib == null) {
            log.debug("No measure lib found for measure: {}", id);
        } else {
            pushLibrary(cqlLib, id);
        }

        return pushMeasure(id, orchestrationProperties);
    }

    private String pushMeasure(String id, OrchestrationProperties orchestrationProperties) {
        boolean validated = measureOrchestrationValidationService.validate(orchestrationProperties);

        if (!validated) {
            throw new HapiResourceValidationException(id, "Measure");
        } else {
            log.debug("Measure {} is valid", id);
        }

        org.hl7.fhir.r4.model.Measure fhirMeasure = orchestrationProperties.getFhirMeasure();
        fhirMeasure.setId(id);
        Narrative humanReadable = new Narrative();
        humanReadable.setStatusAsString("generated");
        fhirMeasure.setText(humanReadable);
        boolean persisted = measureOrchestrationConversionService.convert(orchestrationProperties);

        if (!persisted) {
            throw new HapiResourceValidationException(id, "Measure");
        } else {
            log.debug("Measure {} is valid", id);
        }

        return fhirMeasure.getUrl();
    }





    private void pushLibrary(CqlLibrary cqlLib, String id) {
        String cql = orchestrationService.processCqlLibrary(cqlLib, Boolean.FALSE);

        CqlConversionPayload payload = cqlLibraryTranslationService.convertCqlToJson(cqlLib.getId(),
                new AtomicBoolean(),
                cql,
                CQLLibraryTranslationService.ConversionType.FHIR,
                Boolean.FALSE);

        String json = payload.getJson();
        String xml = payload.getXml();
        ConversionReporter.setFhirElmJson(json, cqlLib.getId());
        ConversionReporter.setFhirElmXml(xml, cqlLib.getId());

        Library lib = libTranslator.translateToFhir(cqlLib.getId(),
                cql,
                xml,
                json);
        hapiFhirServer.persist(lib);
    }


}
