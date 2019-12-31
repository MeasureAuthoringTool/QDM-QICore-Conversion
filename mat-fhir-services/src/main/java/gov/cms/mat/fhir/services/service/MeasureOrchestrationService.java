package gov.cms.mat.fhir.services.service;

import ca.uhn.fhir.validation.ResultSeverityEnum;
import gov.cms.mat.fhir.commons.objects.FhirResourceValidationResult;
import gov.cms.mat.fhir.services.components.fhir.ValueSetFhirValidationResults;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.components.mongo.ConversionResultsService;
import gov.cms.mat.fhir.services.summary.FhirValueSetResourceValidationResult;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MeasureOrchestrationService {
    private final CQLLibraryTranslationService cqlLibraryTranslationService;
    private final ConversionResultsService conversionResultsService;
    private final ValueSetService valueSetService;
    private final ValueSetFhirValidationResults valueSetFhirValidationResults;

    public MeasureOrchestrationService(CQLLibraryTranslationService cqlLibraryTranslationService, ConversionResultsService conversionResultsService, ValueSetService valueSetService, ValueSetFhirValidationResults valueSetFhirValidationResults) {
        this.cqlLibraryTranslationService = cqlLibraryTranslationService;
        this.conversionResultsService = conversionResultsService;
        this.valueSetService = valueSetService;
        this.valueSetFhirValidationResults = valueSetFhirValidationResults;
    }

    public boolean process(OrchestrationProperties properties) {
        ConversionReporter.setInThreadLocal(properties.getMatMeasure().getId(), conversionResultsService);
        ConversionReporter.resetOrchestration();

        processValueSets(properties);


        return false;
    }

    private boolean processValueSets(OrchestrationProperties properties) {
        List<ValueSet> valueSets =
                valueSetService.findValueSetsByMeasure(properties);

        FhirValueSetResourceValidationResult result = valueSetFhirValidationResults.generate(
                valueSets,
                properties.getXmlSource(),
                properties.getMatMeasure().getId());

        if (!resultPass(result)) {
            return false;
        }


        return true;

    }

    private boolean resultPass(FhirValueSetResourceValidationResult result) {

        if (CollectionUtils.isEmpty(result.getFhirResourceValidationResults())) {
            return true;
        } else {
            return result.getFhirResourceValidationResults()
                    .stream()
                    .filter(r -> r.getValidationErrorList().size() > 0)
                    .map(FhirResourceValidationResult::getValidationErrorList)
                    .flatMap(List::stream)
                    .anyMatch(v -> checkSeverity(v.getSeverity()));
        }
    }

    private boolean checkSeverity(String severity) {
        try {
            ResultSeverityEnum resultSeverityEnum = ResultSeverityEnum.valueOf(severity);

            return resultSeverityEnum.equals(ResultSeverityEnum.ERROR) ||
                    resultSeverityEnum.equals(ResultSeverityEnum.FATAL);

        } catch (IllegalArgumentException e) {
            log.error("Cannot find ResultSeverityEnum type from string: {}", severity);
            return false;
        }
    }
}
