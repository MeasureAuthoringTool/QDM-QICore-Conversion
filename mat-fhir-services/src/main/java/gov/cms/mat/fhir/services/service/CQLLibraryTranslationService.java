package gov.cms.mat.fhir.services.service;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.rest.dto.CqlConversionError;
import gov.cms.mat.fhir.rest.dto.MatCqlConversionException;
import gov.cms.mat.fhir.services.components.cql.CqlConversionClient;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.exceptions.CqlConversionException;
import gov.cms.mat.fhir.services.repository.CqlLibraryRepository;
import gov.cms.mat.fhir.services.service.support.ElmErrorExtractor;
import gov.cms.mat.fhir.services.service.support.ErrorSeverityChecker;
import gov.cms.mat.fhir.services.service.support.LibraryConversionReporter;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import gov.cms.mat.fhir.services.translate.creators.FhirLibraryHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static gov.cms.mat.fhir.rest.dto.ConversionOutcome.CQL_LIBRARY_TRANSLATION_FAILED;

@Service
@Slf4j
public class CQLLibraryTranslationService implements ErrorSeverityChecker, LibraryConversionReporter, FhirLibraryHelper {
    private final MeasureDataService measureDataService;
    private final CqlLibraryRepository cqlLibraryRepository;
    private final CqlConversionClient cqlConversionClient;

    public CQLLibraryTranslationService(MeasureDataService measureDataService,
                                        CqlLibraryRepository cqlLibraryRepository,
                                        CqlConversionClient cqlConversionClient) {
        this.measureDataService = measureDataService;
        this.cqlLibraryRepository = cqlLibraryRepository;
        this.cqlConversionClient = cqlConversionClient;
    }

    private boolean process(String id, boolean showWarnings) {
        log.info("CQLLibraryTranslationService processing measure id: {}", id);
        List<CqlLibrary> cqlLibraries = cqlLibraryRepository.getCqlLibraryByMeasureId(id);

        if (cqlLibraries.isEmpty()) {
            return true;
        } else {
            return processLibs(cqlLibraries, showWarnings);
        }
    }

    private boolean processLibs(List<CqlLibrary> cqlLibraries, boolean showWarnings) {
        AtomicBoolean atomicBoolean = new AtomicBoolean(Boolean.TRUE);
        cqlLibraries.forEach(cqlLibrary -> processCqlLibrary(cqlLibrary, atomicBoolean, showWarnings));

        if (!atomicBoolean.get()) {
            ConversionReporter.setTerminalMessage("CQLLibraryTranslationService failed",
                    CQL_LIBRARY_TRANSLATION_FAILED);
        }

        return atomicBoolean.get();
    }

    private void processCqlLibrary(CqlLibrary cqlLibrary, AtomicBoolean atomicBoolean, boolean showWarnings) {
        String cql = convertMatXmlToCql(cqlLibrary.getCqlXml(), cqlLibrary.getId(), showWarnings);
        ConversionReporter.setCql(cql, cqlLibrary.getCqlName(), cqlLibrary.getVersion(), cqlLibrary.getId());

        String json = convertToJson(cqlLibrary, atomicBoolean, cql, ConversionType.QDM, showWarnings);

        String cleanedJson = cleanJsonFromMatExceptions(json);
        ConversionReporter.setElm(cleanedJson, cqlLibrary.getId());
    }

    public String convertToJson(CqlLibrary cqlLibrary,
                                AtomicBoolean atomicBoolean,
                                String cql,
                                ConversionType type,
                                boolean showWarnings) {
        return convertCqlToJson(cqlLibrary == null ? null : cqlLibrary.getId(),
                atomicBoolean,
                cql,
                type,
                showWarnings);

    }

    public String convertCqlToJson(String cqlLibraryId,
                                   AtomicBoolean atomicBoolean,
                                   String cql,
                                   ConversionType type,
                                   boolean showWarnings) {
        String json = convertCqlToJson(cql, showWarnings);

        boolean success = processJsonForError(type, json, cqlLibraryId);

        if (!success) {
            atomicBoolean.set(Boolean.FALSE);
        }

        return json;
    }

    public String convertToJsonFromFhirCql(AtomicBoolean atomicBoolean, String cql, boolean showWarnings) {
        return convertToJson(null, atomicBoolean, cql, ConversionType.FHIR, showWarnings);
    }

    public boolean processJsonForError(ConversionType conversionType, String json, String matLibraryId) {
        ElmErrorExtractor extractor = new ElmErrorExtractor(json);

        List<CqlConversionError> cqlConversionErrors = getCqlConversionErrors(matLibraryId, extractor);
        List<MatCqlConversionException> matCqlConversionExceptions = extractor.parseForErrorExceptions();
        Map<String, List<CqlConversionError>> map = extractor.parseForExternalErrors();
        ConversionReporter.setExternalLibraryErrors(map, matLibraryId);

        if (cqlConversionErrors.isEmpty() && matCqlConversionExceptions.isEmpty()) {
            processCqlConversionResultSuccess(matLibraryId);
            return true;
        } else {
            return checkForErrors(matLibraryId, cqlConversionErrors, matCqlConversionExceptions, conversionType);
        }
    }

    private boolean checkForErrors(String matLibraryId,
                                   List<CqlConversionError> cqlConversionErrors,
                                   List<MatCqlConversionException> matCqlConversionExceptions,
                                   ConversionType conversionType) {
        boolean successFull = true;

        if (!cqlConversionErrors.isEmpty()) {
            processCqlConversionErrors(matLibraryId, conversionType, cqlConversionErrors);

            long errorCount = cqlConversionErrors.stream()
                    .filter(c -> checkSeverity(c.getErrorSeverity()))
                    .count();

            if (errorCount > 0) {
                successFull = false;
            }
        }

        if (!matCqlConversionExceptions.isEmpty()) {
            processMatCqlConversionErrors(matLibraryId, conversionType, matCqlConversionExceptions);

            long errorCount = matCqlConversionExceptions.stream()
                    .filter(c -> checkSeverity(c.getErrorSeverity()))
                    .count();

            if (errorCount > 0) {
                successFull = false;
            }
        }

        return successFull;
    }

    private List<CqlConversionError> getCqlConversionErrors(String matLibraryId, ElmErrorExtractor extractor) {
        try {
            return extractor.parseForAnnotations();
        } catch (CqlConversionException e) {

            if (matLibraryId != null)
                ConversionReporter.setTerminalMessage(e.getMessage(), CQL_LIBRARY_TRANSLATION_FAILED);

            throw e;
        }

    }

    public String convertMatXmlToCql(String cqlXml, String matLibraryId, boolean showWarnings) {
        if (StringUtils.isEmpty(cqlXml)) {
            String message = "CqlXml is missing";

            if (matLibraryId != null) {
                ConversionReporter.setCqlConversionErrorMessage(message, matLibraryId);
            }

            throw new CqlConversionException(message);
        } else {
            return convertToCql(cqlXml, showWarnings);
        }
    }

    public String convertToCql(String xml, boolean showWarnings) {
        try {
            ResponseEntity<String> entity = cqlConversionClient.getCql(xml, showWarnings);
            return entity.getBody();
        } catch (Exception e) {
            log.warn("Error convertToCql", e);
            log.trace(xml);
            throw new CqlConversionException("Cannot convert xml to cql.", e);
        }
    }

    private String convertCqlToJson(String cql, boolean showWarnings) {
        try {
            ResponseEntity<String> entity = cqlConversionClient.getJson(cql, showWarnings);
            return entity.getBody();
        } catch (Exception e) {
            log.warn("Error convertCqlToJson", e);
            log.trace(cql);
            throw new CqlConversionException("Cannot convert cql to json.", e);
        }
    }

    public boolean processOne(String measureId, boolean showWarnings) {
        Measure measure = measureDataService.findOneValid(measureId);
        return process(measure.getId(), showWarnings);
    }

    public boolean validate(OrchestrationProperties properties) {
        return processLibs(properties.getCqlLibraries(), properties.isShowWarnings());
    }

    public enum ConversionType {QDM, FHIR}
}
