package gov.cms.mat.fhir.services.service;


import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.rest.dto.CqlConversionError;
import gov.cms.mat.fhir.rest.dto.MatCqlConversionException;
import gov.cms.mat.fhir.services.components.cql.CqlConversionClient;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.components.mongo.ConversionResultsService;
import gov.cms.mat.fhir.services.exceptions.CqlConversionException;
import gov.cms.mat.fhir.services.repository.CqlLibraryRepository;
import gov.cms.mat.fhir.services.service.support.ElmErrorExtractor;
import gov.cms.mat.fhir.services.service.support.ErrorSeverityChecker;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Slf4j
public class CQLLibraryTranslationService implements ErrorSeverityChecker {
    private static final String CONVERSION_RESULTS_TEMPLATE =
            "Found %d CqlLibraries to process, successfully processed %d";

    private static final String NO_LIBRARIES_FOUND = "No CqlLibraries found for the measure";
    private static final String TOO_MANY_LIBRARIES_FOUND_TEMPLATE = "No CqlLibraries found for the measure count: %d";
    private final MeasureDataService measureDataService;
    private final CqlLibraryRepository cqlLibraryRepository;
    private final CqlConversionClient cqlConversionClient;
    private final ConversionResultsService conversionResultsService;

    public CQLLibraryTranslationService(MeasureDataService measureDataService,
                                        CqlLibraryRepository cqlLibraryRepository,
                                        CqlConversionClient cqlConversionClient,
                                        ConversionResultsService conversionResultsService) {
        this.measureDataService = measureDataService;
        this.cqlLibraryRepository = cqlLibraryRepository;
        this.cqlConversionClient = cqlConversionClient;
        this.conversionResultsService = conversionResultsService;
    }

    public String processAll() {
        List<String> all = measureDataService.findAllValidIds();

        int successfulConversions = all.stream()
                .map(this::processCount)
                .mapToInt(Integer::intValue)
                .sum();

        String resultMessage = String.format(CONVERSION_RESULTS_TEMPLATE, all.size(), successfulConversions);
        log.info("Conversion results: {}", resultMessage);
        return resultMessage;
    }

    private int processCount(String id) {
        try {
            process(id);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    private boolean process(String id) {
        log.info("CQLLibraryTranslationService processing measure id: {}", id);
        ConversionReporter.setInThreadLocal(id, conversionResultsService);

        List<CqlLibrary> cqlLibraries = cqlLibraryRepository.getCqlLibraryByMeasureId(id);

        if (cqlLibraries.isEmpty()) {
            return true;
        } else {
            AtomicBoolean atomicBoolean = new AtomicBoolean(Boolean.TRUE);
            cqlLibraries.forEach(c -> processCqlLibrary(c, atomicBoolean));

            if (!atomicBoolean.get()) {
                ConversionReporter.setErrorMessage("CQLLibraryTranslationService failed");
            }

            return atomicBoolean.get();
        }
    }

    private void processCqlLibrary(CqlLibrary cqlLibrary, AtomicBoolean atomicBoolean) {
        String cql = convertMatXmlToCql(cqlLibrary.getCqlXml(), cqlLibrary.getId());
        ConversionReporter.setCql(cql, cqlLibrary.getId());

        String json = convertCqlToJson(cql);

        boolean success = processJsonForError(json, cqlLibrary.getId());

        if (!success) {
            atomicBoolean.set(Boolean.FALSE);
        }

        ConversionReporter.setElm(json, cqlLibrary.getId());
    }


    private boolean processJsonForError(String json, String matLibraryId) {
        ElmErrorExtractor extractor = new ElmErrorExtractor(json);
        List<CqlConversionError> cqlConversionErrors = extractor.parseForAnnotations();
        List<MatCqlConversionException> matCqlConversionExceptions = extractor.parseForErrorExceptions();

        if (cqlConversionErrors.isEmpty() && matCqlConversionExceptions.isEmpty()) {
            ConversionReporter.setCqlConversionResultSuccess(matLibraryId);
            return true;
        } else {

            boolean successFull = true;

            if (!cqlConversionErrors.isEmpty()) {
                ConversionReporter.setCqlConversionErrorMessage("CQl conversion produced " + cqlConversionErrors.size()
                        + " cqlConversionErrors errors.", matLibraryId);
                ConversionReporter.setCqlConversionErrors(cqlConversionErrors, matLibraryId);

                long errorCount = cqlConversionErrors.stream()
                        .filter(c -> checkSeverity(c.getErrorSeverity()))
                        .count();

                if (errorCount > 0) {
                    successFull = false;
                }
            }

            if (!matCqlConversionExceptions.isEmpty()) {
                ConversionReporter.setCqlConversionErrorMessage("CQl conversion produced " + matCqlConversionExceptions.size()
                        + " matCqlConversionExceptions (errorExceptions) errors.", matLibraryId);
                ConversionReporter.setMatCqlConversionExceptions(matCqlConversionExceptions, matLibraryId);

                long errorCount = matCqlConversionExceptions.stream()
                        .filter(c -> checkSeverity(c.getErrorSeverity()))
                        .count();

                if (errorCount > 0) {
                    successFull = false;
                }
            }

            return successFull;
        }
    }

    private String convertMatXmlToCql(String cqlXml, String matLibraryId) {
        if (StringUtils.isEmpty(cqlXml)) {
            String message = "CqlXml is missing";
            ConversionReporter.setCqlConversionErrorMessage(message, matLibraryId);
            throw new CqlConversionException(message);
        } else {
            return convertToCql(cqlXml);
        }
    }

    private String convertToCql(String xml) {
        try {
            ResponseEntity<String> entity = cqlConversionClient.getCql(xml);
            return entity.getBody();
        } catch (Exception e) {
            log.warn("Error convertToCql", e);
            log.trace(xml);
            throw new CqlConversionException("Cannot convert xml to cql.", e);
        }
    }


    private String convertCqlToJson(String cql) {
        try {
            ResponseEntity<String> entity = cqlConversionClient.getJson(cql);
            return entity.getBody();
        } catch (Exception e) {
            log.warn("Error convertCqlToJson", e);
            log.trace(cql);
            throw new CqlConversionException("Cannot convert cql to json.", e);
        }
    }

    public boolean processOne(String measureId) {
        Measure measure = measureDataService.findOneValid(measureId);
        return process(measure.getId());
    }

    public boolean validate(OrchestrationProperties properties) {
        return process(properties.getMeasureId());
    }
}
