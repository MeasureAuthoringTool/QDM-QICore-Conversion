package gov.cms.mat.fhir.services.service.orchestration;

import ca.uhn.fhir.validation.ResultSeverityEnum;
import gov.cms.mat.fhir.commons.objects.FhirResourceValidationResult;
import gov.cms.mat.fhir.services.components.fhir.ValueSetFhirValidationResults;
import gov.cms.mat.fhir.services.service.ValueSetService;
import gov.cms.mat.fhir.services.summary.FhirValueSetResourceValidationResult;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import gov.cms.mat.fhir.services.translate.ValueSetVsacVerifier;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
class ValueSetOrchestrationService {
    private final ValueSetService valueSetService;
    private final ValueSetFhirValidationResults valueSetFhirValidationResults;
    private final ValueSetVsacVerifier valueSetVsacVerifier;

    ValueSetOrchestrationService(ValueSetService valueSetService,
                                 ValueSetFhirValidationResults valueSetFhirValidationResults,
                                 ValueSetVsacVerifier valueSetVsacVerifier) {
        this.valueSetService = valueSetService;
        this.valueSetFhirValidationResults = valueSetFhirValidationResults;
        this.valueSetVsacVerifier = valueSetVsacVerifier;
    }

    boolean process(OrchestrationProperties properties) {
        List<ValueSet> valueSets =
                valueSetService.findValueSetsByMeasure(properties);


        List<FhirResourceValidationResult> results = valueSetFhirValidationResults.collectResults(valueSets, properties.getMatMeasure().getId());

//        FhirValueSetResourceValidationResult result = valueSetFhirValidationResults.generate(
//                valueSets,
//                properties.getXmlSource(),
//                properties.getMatMeasure().getId());
//
//        if (!resultPass(result)) {
//            log.info("FhirValidationResults failed for measure: {}", properties.getMatMeasure().getId());
//            return false;
//        }


        return true;
    }

    private boolean resultPass(FhirValueSetResourceValidationResult result) {
        if (CollectionUtils.isEmpty(result.getFhirResourceValidationResults())) {
            return true;
        } else {
            return result.getFhirResourceValidationResults()
                    .stream()
                    .filter(r -> !r.getValidationErrorList().isEmpty())
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
