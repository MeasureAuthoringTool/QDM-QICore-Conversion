package gov.cms.mat.fhir.services.service.orchestration;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.rest.dto.ConversionType;
import gov.cms.mat.fhir.services.components.mat.MatXmlException;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.exceptions.*;
import gov.cms.mat.fhir.services.service.CQLLibraryTranslationService;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class OrchestrationService {
    private final ValueSetOrchestrationValidationService valueSetOrchestrationValidationService;
    private final ValueSetOrchestrationConversionService valueSetOrchestrationConversionService;
    private final CQLLibraryTranslationService cqlLibraryTranslationService;
    private final LibraryOrchestrationConversionService libraryOrchestrationConversionService;
    private final LibraryOrchestrationValidationService libraryOrchestrationValidationService;
    private final MeasureOrchestrationValidationService measureOrchestrationValidationService;
    private final MeasureOrchestrationConversionService measureOrchestrationConversionService;

    public OrchestrationService(ValueSetOrchestrationValidationService valueSetOrchestrationValidationService,
                                ValueSetOrchestrationConversionService valueSetOrchestrationConversionService,
                                CQLLibraryTranslationService cqlLibraryTranslationService,
                                LibraryOrchestrationConversionService libraryOrchestrationConversionService,
                                LibraryOrchestrationValidationService libraryOrchestrationValidationService,
                                MeasureOrchestrationValidationService measureOrchestrationValidationService,
                                MeasureOrchestrationConversionService measureOrchestrationConversionService) {
        this.valueSetOrchestrationValidationService = valueSetOrchestrationValidationService;
        this.valueSetOrchestrationConversionService = valueSetOrchestrationConversionService;
        this.cqlLibraryTranslationService = cqlLibraryTranslationService;
        this.libraryOrchestrationConversionService = libraryOrchestrationConversionService;
        this.libraryOrchestrationValidationService = libraryOrchestrationValidationService;
        this.measureOrchestrationValidationService = measureOrchestrationValidationService;
        this.measureOrchestrationConversionService = measureOrchestrationConversionService;
    }

    public boolean process(OrchestrationProperties properties, String vsacGrantingTicket) {
        boolean processPrerequisitesFlag = processPrerequisites(properties, vsacGrantingTicket);

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

    public boolean processPrerequisites(OrchestrationProperties properties, String vsacGrantingTicket) {
        try {
          //  processAndGetValueSets(properties, vsacGrantingTicket);
            processAndGetCqlLibraries(properties);
            processFhirMeasure(properties);
            return true;
        } catch (LibraryConversionException | ValueSetConversionException | MeasureNotFoundException |
                CqlLibraryNotFoundException | MatXmlMarshalException | MatXmlException e) {
            return false;
        } catch (Exception e) {
            log.info("Error for id: {}", properties.getMeasureId(), e);
            return false;
        }
    }

    private void processFhirMeasure(OrchestrationProperties properties) {
        measureOrchestrationConversionService.processExistingFhirMeasure(properties);
    }

    public void processAndGetCqlLibraries(OrchestrationProperties properties) {

        List<CqlLibrary> cqlLibraries = libraryOrchestrationConversionService.getCqlLibrariesNotInHapi(properties);

        cqlLibraries.forEach(this::processCqlLibrary);

        properties.getCqlLibraries()
                .addAll(cqlLibraries);
    }

    private void processCqlLibrary(CqlLibrary cqlLibrary) {

        if (StringUtils.isEmpty(cqlLibrary.getCqlXml())) {
            throw new CqlConversionException("oops");
        }

        String cql = cqlLibraryTranslationService.convertToCql(cqlLibrary.getCqlXml());
        ConversionReporter.setCql(cql, cqlLibrary.getCqlName(), cqlLibrary.getVersion(), cqlLibrary.getId());

        libraryOrchestrationValidationService.processIncludes(cql);
    }


    public void processAndGetValueSets(OrchestrationProperties properties, String vsacGrantingTicket) {
        List<ValueSet> valueSets =
                valueSetOrchestrationValidationService.getValueSetsNotInHapi(properties, vsacGrantingTicket);

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
            return true; //todo mcg
        } else { // we are valid full steam ahead
            log.debug("Validation has passed for measureId: {}", properties.getMeasureId());
            return true;
        }
    }

    public boolean convert(OrchestrationProperties properties) {
        //return valueSetOrchestrationConversionService.convert(properties) &&
         return libraryOrchestrationConversionService.convert(properties) &&
                measureOrchestrationConversionService.convert(properties);
    }

    public boolean validate(OrchestrationProperties properties) {
        //return valueSetOrchestrationValidationService.validate(properties) &
          return cqlLibraryTranslationService.validate(properties) &
                libraryOrchestrationValidationService.validate(properties) &
                measureOrchestrationValidationService.validate(properties);
    }
}
