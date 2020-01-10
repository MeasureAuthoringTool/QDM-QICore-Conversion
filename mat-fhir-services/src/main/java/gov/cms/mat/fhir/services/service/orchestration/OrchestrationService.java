package gov.cms.mat.fhir.services.service.orchestration;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.rest.dto.ConversionType;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.components.mongo.ConversionResultsService;
import gov.cms.mat.fhir.services.service.CQLLibraryTranslationService;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class OrchestrationService {
    private final ConversionResultsService conversionResultsService;
    private final ValueSetOrchestrationValidationService valueSetOrchestrationValidationService;
    private final ValueSetOrchestrationConversionService valueSetOrchestrationConversionService;
    private final CQLLibraryTranslationService cqlLibraryTranslationService;
    private final LibraryOrchestrationConversionService libraryOrchestrationConversionService;

    public OrchestrationService(ConversionResultsService conversionResultsService,
                                ValueSetOrchestrationValidationService valueSetOrchestrationValidationService,
                                ValueSetOrchestrationConversionService valueSetOrchestrationConversionService,
                                CQLLibraryTranslationService cqlLibraryTranslationService,
                                LibraryOrchestrationConversionService libraryOrchestrationConversionService) {
        this.conversionResultsService = conversionResultsService;
        this.valueSetOrchestrationValidationService = valueSetOrchestrationValidationService;
        this.valueSetOrchestrationConversionService = valueSetOrchestrationConversionService;
        this.cqlLibraryTranslationService = cqlLibraryTranslationService;
        this.libraryOrchestrationConversionService = libraryOrchestrationConversionService;
    }

    public boolean process(OrchestrationProperties properties) {
        ConversionReporter.setInThreadLocal(properties.getMatMeasure().getId(), conversionResultsService);
        ConversionReporter.resetOrchestration();

        processAndGetValueSets(properties);
        processAndGetCqlLibraries(properties);

        if (!processValidation(properties)) {
            log.debug("Conversion Stopped due to validation errors measureId: {}", properties.getMeasureId());
            return false;
        } else {
            return processConversion(properties);
        }
    }

    public void processAndGetCqlLibraries(OrchestrationProperties properties) {
        List<CqlLibrary> cqlLibraries = libraryOrchestrationConversionService.getCqlLibrariesNotInHapi(properties);

        properties.getCqlLibraries().addAll(cqlLibraries);
    }

    public void processAndGetValueSets(OrchestrationProperties properties) {
        List<ValueSet> valueSets = valueSetOrchestrationValidationService.getValueSetsNotInHapi(properties);

        properties.getValueSets().addAll(valueSets);
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
            return false;
        } else { // we are valid full steam ahead
            log.debug("Validation has passed for measureId: {}", properties.getMeasureId());
            return convert(properties);
        }
    }

    public boolean convert(OrchestrationProperties properties) {
        return valueSetOrchestrationConversionService.convert(properties) &&
                libraryOrchestrationConversionService.convert(properties);
    }


    public boolean validate(OrchestrationProperties properties) {
        return valueSetOrchestrationValidationService.validate(properties) &&
                cqlLibraryTranslationService.validate(properties);
    }
}
