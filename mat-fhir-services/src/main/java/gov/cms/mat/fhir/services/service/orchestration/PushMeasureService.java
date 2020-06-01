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
import gov.cms.mat.fhir.services.translate.XmlLibraryTranslator;
import gov.cms.mat.fhir.services.translate.creators.FhirCreator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Narrative;
import org.springframework.stereotype.Service;

import java.util.Base64;
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
    private final MeasureExportRepository measureExportRepo;


    public PushMeasureService(MeasureOrchestrationValidationService measureOrchestrationValidationService,
                              MeasureOrchestrationConversionService measureOrchestrationConversionService,
                              OrchestrationService orchestrationService,
                              MeasureDataService measureDataService,
                              CqlLibraryDataService cqlLibraryDataService,
                              HapiFhirServer hapiFhirServer,
                              CQLLibraryTranslationService cqlLibraryTranslationService,
                              MeasureExportRepository measureExportRepo) {
        this.measureOrchestrationValidationService = measureOrchestrationValidationService;
        this.measureOrchestrationConversionService = measureOrchestrationConversionService;

        this.orchestrationService = orchestrationService;
        this.measureDataService = measureDataService;
        this.cqlLibraryDataService = cqlLibraryDataService;
        this.hapiFhirServer = hapiFhirServer;
        this.cqlLibraryTranslationService = cqlLibraryTranslationService;
        this.measureExportRepo = measureExportRepo;
    }

    public String convert(String id, OrchestrationProperties orchestrationProperties) {
        Measure measure = measureDataService.findOneValid(id);
        orchestrationProperties.setMatMeasure(measure);

        List<CqlLibrary> cqlLibraries = cqlLibraryDataService.getCqlLibrariesByMeasureIdRequired(measure.getId());

        if (cqlLibraries.isEmpty()) {
            log.debug("No libraries found for measure: {}", id);
        } else {
            pushLibrary(cqlLibraries);
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
        Narrative humanReadable = findHumanReadable(id);
        fhirMeasure.setText(humanReadable);
        boolean persisted = measureOrchestrationConversionService.convert(orchestrationProperties);

        if (!persisted) {
            throw new HapiResourceValidationException(id, "Measure");
        } else {
            log.debug("Measure {} is valid", id);
        }

        return fhirMeasure.getUrl();
    }

    private Narrative findHumanReadable(String id) {
        var optionalMeasureExport = measureExportRepo.findByMeasureId(id);

        if (optionalMeasureExport.isPresent()) {
            byte[] bytes = optionalMeasureExport.get().getHumanReadable();

            if(ArrayUtils.isEmpty(bytes)) {
                return null;
            } else {
                byte[] encodedText = Base64.getEncoder().encode(bytes);
                Narrative measureText = new Narrative();
                measureText.setStatusAsString("generated");
                measureText.setDivAsString(new String(encodedText));
                return measureText;

            }
        } else {
            return null;
        }
    }

    private void pushLibrary(List<CqlLibrary> cqlLibraries) {
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

        String version = createVersion(cqlLibrary.getVersion(), cqlLibrary.getRevisionNumber());
        Library library = xmlLibraryTranslator.translateToFhir(version);

        hapiFhirServer.persist(library);
    }
}
