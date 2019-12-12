package gov.cms.mat.fhir.services.service;


import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.services.components.cql.CqlConversionClient;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.components.mongo.ConversionResultsService;
import gov.cms.mat.fhir.services.exceptions.CqlConversionException;
import gov.cms.mat.fhir.services.repository.CqlLibraryRepository;
import gov.cms.mat.fhir.services.service.support.MessageExtractor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

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
            ConversionReporter.setCqlConversionError(NO_LIBRARIES_FOUND);
            throw new CqlConversionException(NO_LIBRARIES_FOUND);
        } else {
            if (cqlLibraries.size() > 1) {
                String message = String.format(TOO_MANY_LIBRARIES_FOUND_TEMPLATE, cqlLibraries.size());
                ConversionReporter.setCqlConversionError(message);
                throw new CqlConversionException(message);
            } else {
                ResponseEntity<String> responseEntity = processCqlLibrary(cqlLibraries.get(0));

                if (responseEntity != null) {
                    ConversionReporter.setCqlConversionResultSuccess();
                    return responseEntity.getBody();
                } else {
                    throw new CqlConversionException("Response entity is null");
                }
            }
        }
    }

    private ResponseEntity<String> processCqlLibrary(CqlLibrary c) {
        if (StringUtils.isEmpty(c.getCqlXml())) {
            ConversionReporter.setCqlConversionError("CqlXml is missing");
            return null;
        } else {
            return convertCqlLibrary(c.getCqlXml());
        }
    }


    //"{"timestamp":"2019-12-11T19:32:47.511+0000","status":422,"error":"Unprocessable Entity","message":"ErrorSeverity: Error, Message: Could not load source for library MATGlobalCommonFunctions, version 4.0.000.,/nErrorSeverity: Error, Message: Could not resolve library name Global.,/nErrorSeverity: Error, Message: Could not determine signature for invocation of operator System.GreaterOrEqual.,/nErrorSeverity: Error, Message: Expected an expression of type 'System.Boolean', but found an expression of type '<unknown>'.,/nErrorSeverity: Error, Message: org.hl7.elm.r1.Null cannot be cast to org.hl7.elm.r1.RelationshipClause,/nErrorSeverity: Error, Message: Could not validate reference to expression Hospital Based Encounter with Age Greater than or Equal to 18 because its definition contains errors.,/nErrorSeverity: Error, Message: Member relevantPeriod not found for type null.,/nErrorSeverity: Error, Message: Could not determine signature for invocation of operator System.IncludedIn.,/nErrorSeverity: Error, Message: Member relevantPeriod not found for type null.,/nErrorSeverity: Error, Message: Could not determine signature for invocation of operator System.Overlaps.,/nErrorSeverity: Error, Message: Could not determine signature for invocation of operator System.Or.,/nErrorSeverity: Error, Message: Could not validate reference to expression Hospital Based Encounter with Age Greater than or Equal to 18 because its definition contains errors.,/nErrorSeverity: Error, Message: Member relevantPeriod not found for type null.,/nErrorSeverity: Error, Message: Could not determine signature for invocation of operator System.IncludedIn.,/nErrorSeverity: Error, Message: Member relevantPeriod not found for type null.,/nErrorSeverity: Error, Message: Could not determine signature for invocation of operator System.Overlaps.,/nErrorSeverity: Error, Message: Could not determine signature for invocation of operator System.Or.,/nErrorSeverity: Error, Message: Expected an expression of type 'System.Boolean', but found an expression of type '<unknown>'.,/nErrorSeverity: Error, Message: org.hl7.elm.r1.Null cannot be cast to org.hl7.elm.r1.RelationshipClause,/nErrorSeverity: Error, Message: Could not determine signature for invocation of operator System.Exists.,/nErrorSeverity: Error, Message: Could not determine signature for invocation of operator System.Or.,/nErrorSeverity: Error, Message: Expected an expression of type 'System.Boolean', but found an expression of type '<unknown>'.,/nErrorSeverity: Error, Message: org.hl7.elm.r1.Null cannot be cast to org.hl7.elm.r1.RelationshipClause,/nErrorSeverity: Error, Message: Could not validate reference to expression Encounters with Active Opioids or Benzodiazepines because its definition contains errors.,/nErrorSeverity: Error, Message: Could not validate reference to expression Hospital Based Encounter with Age Greater than or Equal to 18 because its definition contains errors.,/nErrorSeverity: Error, Message: Member relevantPeriod not found for type null.,/nErrorSeverity: Error, Message: Could not determine signature for invocation of operator System.IncludedIn.,/nErrorSeverity: Error, Message: Expected an expression of type 'System.Boolean', but found an expression of type '<unknown>'.,/nErrorSeverity: Error, Message: org.hl7.elm.r1.Null cannot be cast to org.hl7.elm.r1.RelationshipClause,/nErrorSeverity: Error, Message: Could not validate reference to expression Hospital Based Encounter with Age Greater than or Equal to 18 because its definition contains errors.,/nErrorSeverity: Error, Message: Member relevantPeriod not found for type null.,/nErrorSeverity: Error, Message: Could not determine signature for invocation of operator System.IncludedIn.,/nErrorSeverity: Error, Message: Expected an expression of type 'System.Boolean', but found an expression of type '<unknown>'.,/nErrorSeverity: Error, Message: org.hl7.elm.r1.Null cannot be cast to org.hl7.elm.r1.RelationshipClause,/nErrorSeverity: Error, Message: Could not determine signature for invocation of operator System.Union.,/nErrorSeverity: Error, Message: Could not validate reference to expression Encounter Discharges with Opioids or Benzodiazepines because its definition contains errors.,/nErrorSeverity: Error, Message: Could not determine signature for invocation of operator System.Union.,/nErrorSeverity: Error, Message: Could not validate reference to expression Initial Population because its definition contains errors.,/nErrorSeverity: Error, Message: Could not validate reference to expression Hospital Based Encounter with Age Greater than or Equal to 18 because its definition contains errors.,/nErrorSeverity: Error, Message: Could not validate reference to expression Hospital Based Encounter with Age Greater than or Equal to 18 because its definition contains errors.,/nErrorSeverity: Error, Message: Member relevantPeriod not found for type null.,/nErrorSeverity: Error, Message: Could not determine signature for invocation of operator System.OverlapsAfter.,/nErrorSeverity: Error, Message: Member relevantPeriod not found for type null.,/nErrorSeverity: Error, Message: Could not determine signature for invocation of operator System.OverlapsAfter.,/nErrorSeverity: Error, Message: Expected an expression of type 'System.Boolean', but found an expression of type '<unknown>'.,/nErrorSeverity: Error, Message: Could not determine signature for invocation of operator System.And.,/nErrorSeverity: Error, Message: Expected an expression of type 'System.Boolean', but found an expression of type '<unknown>'.,/nErrorSeverity: Error, Message: org.hl7.elm.r1.Null cannot be cast to org.hl7.elm.r1.RelationshipClause,/nErrorSeverity: Error, Message: Could not determine signature for invocation of operator System.Union.,/nErrorSeverity: Error, Message: Could not determine signature for invocation of operator System.Union.,/nErrorSeverity: Error, Message: Could not determine signature for invocation of operator System.Union.,/nErrorSeverity: Error, Message: Could not determine signature for invocation of operator System.Union.,/nErrorSeverity: Error, Message: Could not validate reference to expression Combination of Opiates and Benzodiazepines at Discharge because its definition contains errors.,/nErrorSeverity: Error, Message: Member authorDatetime not found for type null.,/nErrorSeverity: Error, Message: Member relevantPeriod not found for type null.,/nErrorSeverity: Error, Message: Could not determine signature for invocation of operator System.IncludedIn.,/nErrorSeverity: Error, Message: Expected an expression of type 'System.Boolean', but found an expression of type '<unknown>'.,/nErrorSeverity: Error, Message: org.hl7.elm.r1.Null cannot be cast to org.hl7.elm.r1.RelationshipClause,/nErrorSeverity: Error, Message: Could not validate reference to expression Hospital Based Encounter with Age Greater than or Equal to 18 because its definition contains errors.,/nErrorSeverity: Error, Message: Could not validate reference to expression Combination of Opiates and Benzodiazepines at Discharge because its definition contains errors.,/nErrorSeverity: Error, Message: Member relevantPeriod not found for type null.,/nErrorSeverity: Error, Message: Member relevantPeriod not found for type null.,/nErrorSeverity: Error, Message: Could not determine signature for invocation of operator System.Overlaps.,/nErrorSeverity: Error, Message: Member relevantPeriod not found for type null.,/nErrorSeverity: Error, Message: Member relevantPeriod not found for type null.,/nErrorSeverity: Error, Message: Could not determine signature for invocation of operator System.OverlapsAfter.,/nErrorSeverity: Error, Message: Could not determine signature for invocation of operator System.Or.,/nErrorSeverity: Error, Message: Expected an expression of type 'System.Boolean', but found an expression of type '<unknown>'.,/nErrorSeverity: Error, Message: org.hl7.elm.r1.Null cannot be cast to org.hl7.elm.r1.RelationshipClause,/nErrorSeverity: Error, Message: Could not determine signature for invocation of operator System.Union.,/nErrorSeverity: Error, Message: Could not validate reference to expression Opiates and Benzodiazepines at Discharge because its definition contains errors.,/nErrorSeverity: Error, Message: Could not determine signature for invocation of operator System.Union.","path":"/cql/translator/xml"}"

    private ResponseEntity<String> convertCqlLibrary(String cqlXml) {
        try {
            return cqlConversionClient.getData(cqlXml);
        } catch (HttpClientErrorException e) {
            String body = e.getResponseBodyAsString();
            String message = new MessageExtractor(body).parse();

            if (StringUtils.isEmpty(body)) {
                ConversionReporter.setCqlConversionError("Error did not contain a body");
            } else {
                if (e.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
                    List<String> messages = new MessageExtractor(body).parseMany();
                    messages.forEach(ConversionReporter::setCqlConversionError);
                } else {

                    ConversionReporter.setCqlConversionError(e.getStatusCode() + " - " + message);
                }
            }

            throw new CqlConversionException(message);
        }
    }

    public String processOne(String measureId) {
        Measure measure = measureService.findOneValid(measureId);
        return process(measure.getId());
    }
}
