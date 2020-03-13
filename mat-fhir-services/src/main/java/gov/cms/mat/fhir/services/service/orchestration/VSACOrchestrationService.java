package gov.cms.mat.fhir.services.service.orchestration;

import gov.cms.mat.fhir.rest.dto.ConversionType;
import gov.cms.mat.fhir.services.components.mat.MatXmlException;
import gov.cms.mat.fhir.services.exceptions.*;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class VSACOrchestrationService {
    private final ValueSetOrchestrationValidationService valueSetOrchestrationValidationService;
    private final ValueSetOrchestrationConversionService valueSetOrchestrationConversionService;


    public VSACOrchestrationService(ValueSetOrchestrationValidationService valueSetOrchestrationValidationService,
                                    ValueSetOrchestrationConversionService valueSetOrchestrationConversionService) {
        this.valueSetOrchestrationValidationService = valueSetOrchestrationValidationService;
        this.valueSetOrchestrationConversionService = valueSetOrchestrationConversionService;
    }

    public boolean process(OrchestrationProperties properties) {
        boolean processPrerequisitesFlag = processPrerequisites(properties);

        if (!processPrerequisitesFlag) {
            log.debug("Conversion Stopped due to Prerequisites failures measureId: {}", properties.getMeasureId());
            return false;
        } else if (!processValidation(properties)) {
            log.debug("Conversion Stopped due to validation errors measureId: {}", properties.getMeasureId());
            return false;
        } else {
            return processConversion(properties);
        }
    }

    public boolean processPrerequisites(OrchestrationProperties properties) {
        try {
            processAndGetValueSets(properties);
            return true;
        } catch (LibraryConversionException | ValueSetConversionException | MeasureNotFoundException |
                CqlLibraryNotFoundException | MatXmlMarshalException | MatXmlException e) {
            return false;
        } catch (Exception e) {
            log.info("Error for id: {}", properties.getMeasureId(), e);
            return false;
        }
    }


    public void processAndGetValueSets(OrchestrationProperties properties) {
        List<ValueSet> valueSets = valueSetOrchestrationValidationService.getValueSetsNotInHapi(properties);

        properties.getValueSets()
                .addAll(valueSets);
    }

    /* Should be called only when validation has succeeded */
    public boolean processConversion(OrchestrationProperties properties) {
        if (properties.getConversionType().equals(ConversionType.VALIDATION)) {
            log.debug("Conversion not requested for measureId: {}", properties.getMeasureId());
            return true;
        } else {
            log.debug("Conversion has started for measureId: {}, xmlSource: {} and conversionType: {}",
                    properties.getMeasureId(), properties.getXmlSource(), properties.getConversionType());

            boolean converted = convert(properties);

            if (!converted) {
                log.debug("Conversion has failed for measureId: {}", properties.getMeasureId());
            } else {
                log.debug("Conversion has passed for measureId: {}", properties.getMeasureId());
            }

            return converted;
        }
    }

    public boolean processValidation(OrchestrationProperties properties) {
        log.debug("Validation has started for measureId: {}, xmlSource: {} and conversionType: {}",
                properties.getMeasureId(), properties.getXmlSource(), properties.getConversionType());

        boolean validated = validate(properties);

        if (!validated) {
            log.debug("Validation has failed for measureId: {}", properties.getMeasureId());
            log.warn("Validation has failed for measureId: {} BUT CONVERTING ANYWAY", properties.getMeasureId());
            return true; //todo mcg ??/
        } else { // we are valid full steam ahead
            log.debug("Validation has passed for measureId: {}", properties.getMeasureId());
            return true;
        }
    }

    public boolean convert(OrchestrationProperties properties) {
        return valueSetOrchestrationConversionService.convert(properties);

    }

    public boolean validate(OrchestrationProperties properties) {
        return valueSetOrchestrationValidationService.validate(properties);
    }
}
