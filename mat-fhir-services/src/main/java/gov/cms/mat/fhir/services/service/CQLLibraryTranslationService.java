package gov.cms.mat.fhir.services.service;


import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.services.components.cql.CqlConversionClient;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.components.mongo.ConversionResultsService;
import gov.cms.mat.fhir.services.exceptions.CqlConversionException;
import gov.cms.mat.fhir.services.repository.CqlLibraryRepository;
import gov.cms.mat.fhir.services.service.support.CqlConversionError;
import gov.cms.mat.fhir.services.service.support.ElmErrorExtractor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class CQLLibraryTranslationService {
    private static final String CONVERSION_RESULTS_TEMPLATE =
            "Found %d CqlLibraries to process, successfully processed %d";

    private static final String NO_LIBRARIES_FOUND = "No CqlLibraries found for the measure";
    private static final String TOO_MANY_LIBRARIES_FOUND_TEMPLATE = "No CqlLibraries found for the measure count: %d";
    private final MeasureService measureService;
    private final CqlLibraryRepository cqlLibraryRepository;
    private final CqlConversionClient cqlConversionClient;
    private final ConversionResultsService conversionResultsService;

    public CQLLibraryTranslationService(MeasureService measureService,
                                        CqlLibraryRepository cqlLibraryRepository, CqlConversionClient cqlConversionClient, ConversionResultsService conversionResultsService) {
        this.measureService = measureService;
        this.cqlLibraryRepository = cqlLibraryRepository;
        this.cqlConversionClient = cqlConversionClient;
        this.conversionResultsService = conversionResultsService;
    }

    public String processAll() {
        List<String> all = measureService.findAllValidIds();

        int successfulConversions = all.stream()
                .map(this::processCount)
                .mapToInt(Integer::intValue)
                .sum();

        return String.format(CONVERSION_RESULTS_TEMPLATE, all.size(), successfulConversions);
    }

    private int processCount(String id) {
        try {
            process(id);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    private String process(String id) {
        log.info("Processing measure id: {}", id);
        ConversionReporter.setInThreadLocal(id, conversionResultsService);
        ConversionReporter.resetCqlConversionResult();

        List<CqlLibrary> cqlLibraries = cqlLibraryRepository.getCqlLibraryByMeasureId(id);

        if (cqlLibraries.isEmpty()) {
            ConversionReporter.setCqlConversionErrorMessage(NO_LIBRARIES_FOUND);
            throw new CqlConversionException(NO_LIBRARIES_FOUND);
        } else {
            if (cqlLibraries.size() > 1) {
                String message = String.format(TOO_MANY_LIBRARIES_FOUND_TEMPLATE, cqlLibraries.size());
                ConversionReporter.setCqlConversionErrorMessage(message);
                throw new CqlConversionException(message);
            } else {
                String cql = convertMatXmlToCql(cqlLibraries.get(0).getCqlXml());
                ConversionReporter.setCql(cql);

                String json = convertCqlToJson(cql);

                processJsonForError(json);
                ConversionReporter.setCqlConversionResultSuccess();

                return json;
            }
        }
    }

    private void processJsonForError(String json) {
        ElmErrorExtractor extractor = new ElmErrorExtractor(json);
        List<CqlConversionError> errors = extractor.parse();

        if (errors.isEmpty()) {
            ConversionReporter.setCqlConversionResultSuccess();
        } else {
            ConversionReporter.setCqlConversionErrorMessage("CQl conversion produced " + errors.size() + " errors.");
            ConversionReporter.setCqlConversionErrors(errors);
        }
    }

    private String convertMatXmlToCql(String cqlXml) {
        if (StringUtils.isEmpty(cqlXml)) {
            String message = "CqlXml is missing";
            ConversionReporter.setCqlConversionErrorMessage(message);
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
            log.trace(xml);
            throw new CqlConversionException("Cannot convert xml to cql.", e);
        }
    }


    private String convertCqlToJson(String cql) {
        try {
            ResponseEntity<String> entity = cqlConversionClient.getJson(cql);
            return entity.getBody();
        } catch (Exception e) {
            log.trace(cql);
            throw new CqlConversionException("Cannot convert cql to json.", e);
        }
    }


    public String processOne(String measureId) {
        Measure measure = measureService.findOneValid(measureId);
        return process(measure.getId());
    }
}
