package gov.cms.mat.fhir.services.service.orchestration;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.rest.dto.ConversionType;
import gov.cms.mat.fhir.services.components.mat.DraftMeasureXmlProcessor;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.exceptions.CqlConversionException;
import gov.cms.mat.fhir.services.service.CQLLibraryTranslationService;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LibraryOrchestrationService {
    private final CQLLibraryTranslationService cqlLibraryTranslationService;
    private final LibraryOrchestrationConversionService libraryOrchestrationConversionService;
    private final LibraryOrchestrationValidationService libraryOrchestrationValidationService;
    private final MeasureOrchestrationConversionService measureOrchestrationConversionService;
    private final DraftMeasureXmlProcessor draftMeasureXmlProcessor;

    public LibraryOrchestrationService(CQLLibraryTranslationService cqlLibraryTranslationService,
                                       LibraryOrchestrationConversionService libraryOrchestrationConversionService,
                                       LibraryOrchestrationValidationService libraryOrchestrationValidationService,
                                       MeasureOrchestrationConversionService measureOrchestrationConversionService,
                                       DraftMeasureXmlProcessor draftMeasureXmlProcessor) {

        this.cqlLibraryTranslationService = cqlLibraryTranslationService;
        this.libraryOrchestrationConversionService = libraryOrchestrationConversionService;
        this.libraryOrchestrationValidationService = libraryOrchestrationValidationService;
        this.measureOrchestrationConversionService = measureOrchestrationConversionService;
        this.draftMeasureXmlProcessor = draftMeasureXmlProcessor;
    }

    public boolean process(OrchestrationProperties properties) {
        if (!processValidation(properties)) {
            log.debug("Conversion Stopped due to validation errors measureId: {}", properties.getMeasureId());
            return false;
        } else {
            return processConversion(properties);
        }
    }

//    public boolean processPrerequisites(OrchestrationProperties properties) {
//        try {
//            if (properties.getXmlSource() == XmlSource.SIMPLE) {
//                processAndGetCqlLibraries(properties);
//            } else {
//                processDraft(properties);
//            }
//            return true;
//        } catch (LibraryConversionException | ValueSetConversionException | MeasureNotFoundException | NoCqlLibrariesFoundException |
//                CqlLibraryNotFoundException | MatXmlMarshalException | MatXmlException e) {
//            log.info("Error for id: " + properties.getMeasureId(), e);
//            return false;
//        } catch (Exception e) {
//            log.info("Error for id: {}", properties.getMeasureId(), e);
//            return false;
//        }
//    }


//    private void processFhirMeasure(OrchestrationProperties properties) {
//        measureOrchestrationConversionService.processExistingFhirMeasure(properties);
//    }
//
//    private void processDraft(OrchestrationProperties properties) {
//        draftMeasureXmlProcessor.process(properties.getMatMeasure(), properties.isShowWarnings());
//    }
//
//    public void processAndGetCqlLibraries(OrchestrationProperties properties) {
//
//        List<CqlLibrary> cqlLibraries = libraryOrchestrationConversionService.getCqlLibrariesNotInHapi(properties);
//
//        cqlLibraries.forEach(c -> processCqlLibrary(c, properties.isShowWarnings()));
//
//        properties.getCqlLibraries()
//                .addAll(cqlLibraries);
//    }

    private void processCqlLibrary(CqlLibrary cqlLibrary, boolean showWarnings) {

        if (StringUtils.isEmpty(cqlLibrary.getCqlXml())) {
            throw new CqlConversionException("Cql Xml is blank for library : " + cqlLibrary.getCqlName());
        }

        String cql = cqlLibraryTranslationService.convertToCql(cqlLibrary.getCqlXml(), showWarnings);
        ConversionReporter.setCql(cql, cqlLibrary.getCqlName(), cqlLibrary.getVersion(), cqlLibrary.getId());

        libraryOrchestrationValidationService.processIncludes(cql, showWarnings);
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
        return cqlLibraryTranslationService.validate(properties) &&
                libraryOrchestrationValidationService.validate(properties);
    }
}
