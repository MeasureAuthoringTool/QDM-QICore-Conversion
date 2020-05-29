package gov.cms.mat.fhir.services.service.orchestration;

import gov.cms.mat.cql.dto.CqlConversionPayload;
import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.exceptions.HapiResourceValidationException;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.service.CQLLibraryTranslationService;
import gov.cms.mat.fhir.services.service.CqlLibraryDataService;
import gov.cms.mat.fhir.services.service.MeasureDataService;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import gov.cms.mat.fhir.services.translate.XmlLibraryTranslator;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Library;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Slf4j
public class PushMeasureService {

    private final MeasureOrchestrationValidationService measureOrchestrationValidationService;
    private final MeasureOrchestrationConversionService measureOrchestrationConversionService;
    private final OrchestrationService orchestrationService;

    private final MeasureDataService measureDataService;

    private final CqlLibraryDataService cqlLibraryDataService;
    private final HapiFhirServer hapiFhirServer;
    private final CQLLibraryTranslationService cqlLibraryTranslationService;

    public PushMeasureService(MeasureOrchestrationValidationService measureOrchestrationValidationService,
                              MeasureOrchestrationConversionService measureOrchestrationConversionService,
                              OrchestrationService orchestrationService,
                              MeasureDataService measureDataService,
                              CqlLibraryDataService cqlLibraryDataService,
                              HapiFhirServer hapiFhirServer,
                              CQLLibraryTranslationService cqlLibraryTranslationService) {
        this.measureOrchestrationValidationService = measureOrchestrationValidationService;
        this.measureOrchestrationConversionService = measureOrchestrationConversionService;

        this.orchestrationService = orchestrationService;
        this.measureDataService = measureDataService;
        this.cqlLibraryDataService = cqlLibraryDataService;
        this.hapiFhirServer = hapiFhirServer;
        this.cqlLibraryTranslationService = cqlLibraryTranslationService;
    }

    public String convert(String id, OrchestrationProperties orchestrationProperties) {
        Measure measure = measureDataService.findOneValid(id);
        orchestrationProperties.setMatMeasure(measure);

        List<CqlLibrary> cqlLibraries = cqlLibraryDataService.getCqlLibrariesByMeasureIdRequired(measure.getId());

        if (cqlLibraries.isEmpty()) {
            log.debug("No libraries found for measure: {}", id);
        } else {
            CqlLibrary cqlLibrary = cqlLibraries.get(0);

            String cql = orchestrationService.processCqlLibrary(cqlLibrary, Boolean.FALSE);

            CqlConversionPayload payload = cqlLibraryTranslationService.convertCqlToJson(cqlLibrary.getId(),
                    new AtomicBoolean(),
                    cql,
                    CQLLibraryTranslationService.ConversionType.FHIR,
                    Boolean.FALSE);

            String json = payload.getJson();
            String xml = payload.getXml();
            ConversionReporter.setFhirElmJson(json, cqlLibrary.getId());
            ConversionReporter.setFhirElmXml(xml, cqlLibrary.getId());

            XmlLibraryTranslator xmlLibraryTranslator = new XmlLibraryTranslator(cqlLibrary.getCqlName(),
                    cql,
                    json,
                    xml,
                    hapiFhirServer.getBaseURL(),
                    cqlLibrary.getId());
            Library library = xmlLibraryTranslator.translateToFhir(cqlLibrary.getVersion().toString());

            hapiFhirServer.persist(library);
        }

        boolean validated = measureOrchestrationValidationService.validate(orchestrationProperties);

        if (!validated) {
            throw new HapiResourceValidationException(id, "Measure");
        } else {
            log.debug("Measure {} is valid", id);
        }

        orchestrationProperties.getFhirMeasure().setId(id); // todo mcg verify if we need and if better way
        boolean persisted = measureOrchestrationConversionService.convert(orchestrationProperties);

        if (!persisted) {
            throw new HapiResourceValidationException(id, "Measure");
        } else {
            log.debug("Measure {} is valid", id);
        }

        return orchestrationProperties.getFhirMeasure().getUrl();
    }
}
