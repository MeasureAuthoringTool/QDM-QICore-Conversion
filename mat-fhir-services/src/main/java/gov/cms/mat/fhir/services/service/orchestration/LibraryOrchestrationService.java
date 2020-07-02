package gov.cms.mat.fhir.services.service.orchestration;

import gov.cms.mat.fhir.rest.dto.ConversionType;
import gov.cms.mat.fhir.services.service.CQLLibraryTranslationService;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LibraryOrchestrationService {
    private final CQLLibraryTranslationService cqlLibraryTranslationService;
    private final LibraryOrchestrationConversionService libraryOrchestrationConversionService;
    private final LibraryOrchestrationValidationService libraryOrchestrationValidationService;

    public LibraryOrchestrationService(CQLLibraryTranslationService cqlLibraryTranslationService,
                                       LibraryOrchestrationConversionService libraryOrchestrationConversionService,
                                       LibraryOrchestrationValidationService libraryOrchestrationValidationService) {

        this.cqlLibraryTranslationService = cqlLibraryTranslationService;
        this.libraryOrchestrationConversionService = libraryOrchestrationConversionService;
        this.libraryOrchestrationValidationService = libraryOrchestrationValidationService;

    }

    public boolean process(OrchestrationProperties properties) {
        if (!processValidation(properties)) {
            log.debug("Conversion Stopped due to validation errors measureId: {}", properties.getMeasureId());
            return false;
        } else {
            return processConversion(properties);
        }
    }


    /* Should be called only when validation has succeeded */
    public boolean processConversion(OrchestrationProperties properties) {
        if (properties.getConversionType().equals(ConversionType.VALIDATION)) {
            log.debug("Conversion not requested for measureId: {}", properties.getMeasureId());
            return true;
        } else {
            log.debug("Conversion has started for measureId: {}, xmlSource: {} and conversionType: {}",
                    properties.getMeasureId(), properties.getXmlSource(), properties.getConversionType());

            if (properties.isPush()) {
                boolean converted = convert(properties);
                if (!converted) {
                    log.debug("Conversion has failed for measureId: {}", properties.getMeasureId());
                } else {
                    log.debug("Conversion has passed for measureId: {}", properties.getMeasureId());
                }
                return converted;
            } else {
                return true;
            }
        }
    }

    public boolean processValidation(OrchestrationProperties properties) {

        boolean validated = validate(properties);

        if (!validated) {
            log.debug("Validation has failed for measureId: {}", properties.getMeasureId());
            log.warn("Validation has failed for measureId: {} BUT CONVERTING ANYWAY", properties.getMeasureId());
            return true; //todo mcg
        } else { // we are valid full steam ahead
            log.debug("Validation has passed for measureId: {}", properties.getMeasureId());
            return true;
        }
    }

    public boolean convert(OrchestrationProperties properties) {
        return libraryOrchestrationConversionService.convert(properties);
    }

    public boolean validate(OrchestrationProperties properties) {
        return cqlLibraryTranslationService.validate(properties) &
                libraryOrchestrationValidationService.validate(properties);
    }
}
