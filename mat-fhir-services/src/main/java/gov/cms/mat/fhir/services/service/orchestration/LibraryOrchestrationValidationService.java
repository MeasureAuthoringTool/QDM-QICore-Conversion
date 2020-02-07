package gov.cms.mat.fhir.services.service.orchestration;

import gov.cms.mat.cql.CqlParser;
import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.rest.dto.FhirValidationResult;
import gov.cms.mat.fhir.rest.dto.LibraryConversionResults;
import gov.cms.mat.fhir.services.components.library.UnConvertedCqlLibraryFileHandler;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.components.mongo.ConversionResult;
import gov.cms.mat.fhir.services.exceptions.LibraryConversionException;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.rest.support.CqlVersionConverter;
import gov.cms.mat.fhir.services.rest.support.FhirValidatorProcessor;
import gov.cms.mat.fhir.services.service.CQLLibraryTranslationService;
import gov.cms.mat.fhir.services.service.CqlLibraryDataService;
import gov.cms.mat.fhir.services.service.support.ErrorSeverityChecker;
import gov.cms.mat.fhir.services.summary.CqlLibraryFindData;
import gov.cms.mat.fhir.services.summary.FhirLibraryResourceValidationResult;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import gov.cms.mat.fhir.services.translate.LibraryTranslator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Library;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static gov.cms.mat.fhir.rest.dto.ConversionOutcome.CQL_LIBRARY_TRANSLATION_FAILED;
import static gov.cms.mat.fhir.rest.dto.ConversionOutcome.LIBRARY_VALIDATION_FAILED;

@Component
@Slf4j
public class LibraryOrchestrationValidationService extends LibraryOrchestrationBase
        implements FhirValidatorProcessor, ErrorSeverityChecker, CqlVersionConverter {

    private static final String VALIDATION_FAILURE_MESSAGE = "Library validation failed";
    private static final String HAPI_FAILURE_MESSAGE = "Cannot find hapi fhir library with name: %s and version: %s";

    private final CqlLibraryDataService cqlLibraryDataService;
    private final CQLLibraryTranslationService cqlLibraryTranslationService;

    private final UnConvertedCqlLibraryFileHandler unConvertedCqlLibraryFileHandler;

    public LibraryOrchestrationValidationService(HapiFhirServer hapiFhirServer,
                                                 CqlLibraryDataService cqlLibraryDataService,
                                                 CQLLibraryTranslationService cqlLibraryTranslationService,
                                                 UnConvertedCqlLibraryFileHandler unConvertedCqlLibraryFileHandler) {
        super(hapiFhirServer);
        this.cqlLibraryDataService = cqlLibraryDataService;
        this.cqlLibraryTranslationService = cqlLibraryTranslationService;
        this.unConvertedCqlLibraryFileHandler = unConvertedCqlLibraryFileHandler;
    }

    public void processIncludedLibrary(CqlParser.IncludeProperties include, CqlParser.UsingProperties using) {

        Bundle bundle = hapiFhirServer.getLibraryBundleByVersionAndName(include.getVersion(), include.getName());

        if (CollectionUtils.isEmpty(bundle.getEntry())) {
            CqlLibraryFindData data = CqlLibraryFindData.builder()
                    .qdmVersion(using.getVersion())
                    .name(include.getName())
                    .matVersion(convertVersionToBigDecimal(include.getVersion()))
                    .version(include.getVersion())
                    .build();

            if (unConvertedCqlLibraryFileHandler.exists(data)) {
                log.info("File already exists for: {}", unConvertedCqlLibraryFileHandler.makeCqlFileName(data));
            } else {
                CqlLibrary cqlLibrary = cqlLibraryDataService.findCqlLibrary(data);
                String cql = cqlLibraryTranslationService.convertMatXmlToCql(cqlLibrary.getCqlXml(), null);
                unConvertedCqlLibraryFileHandler.write(data, cql);
            }

            String message = String.format(HAPI_FAILURE_MESSAGE, include.getName(), include.getVersion());
            ConversionReporter.setTerminalMessage(message, CQL_LIBRARY_TRANSLATION_FAILED);
            throw new LibraryConversionException(message);
        } else {
            log.debug("Included Library already in fhir: {}", include);
        }

    }

    public void processIncludes(String cql) {
        CqlParser cqlParser = new CqlParser(cql);
        List<CqlParser.IncludeProperties> includes = cqlParser.getIncludes();
        CqlParser.UsingProperties using = cqlParser.getUsing();

        // when no more includes recursion will stop
        includes.forEach(includeProperties -> processIncludedLibrary(includeProperties, using));
    }

    boolean validate(OrchestrationProperties properties) {

        List<Library> libraryList = properties.getCqlLibraries().stream()
                .map(this::translateCqlLib)
                .collect(Collectors.toList());

        properties.getFhirLibraries().addAll(libraryList);

        AtomicBoolean atomicBoolean = new AtomicBoolean(true);

        properties.getCqlLibraries()
                .forEach(matLib -> validate(matLib, properties.findFhirLibrary(matLib.getId()), atomicBoolean));

        if (!atomicBoolean.get()) {
            ConversionReporter.setTerminalMessage(VALIDATION_FAILURE_MESSAGE, LIBRARY_VALIDATION_FAILED);
        }

        return atomicBoolean.get();
    }

    private Library translateCqlLib(CqlLibrary cqlLibrary) {
        ConversionResult conversionResult = ConversionReporter.getConversionResult();
        LibraryConversionResults results = conversionResult.findLibraryConversionResultsRequired(cqlLibrary.getId());

        LibraryTranslator libraryTranslator = new LibraryTranslator(cqlLibrary,
                results.getCqlConversionResult().getElm().getBytes(),
                results.getCqlConversionResult().getCql().getBytes(),
                hapiFhirServer.getBaseURL());

        Library fhirLibrary = libraryTranslator.translateToFhir(null);

        results.setFhirLibraryJson(hapiFhirServer.toJson(fhirLibrary));

        ConversionReporter.saveConversionResult(conversionResult);

        return fhirLibrary;
    }


    private FhirLibraryResourceValidationResult validate(CqlLibrary matCqlLibrary, Library fhirLibrary, AtomicBoolean atomicBoolean) {

        FhirLibraryResourceValidationResult response = new FhirLibraryResourceValidationResult(matCqlLibrary.getId());
        response.setMeasureId(matCqlLibrary.getMeasureId());

        log.info("VALIDATE-RESOURCE-START Library");
        validateResource(response, fhirLibrary, hapiFhirServer.getCtx());
        log.info("VALIDATE-RESOURCE-END Library");

        List<FhirValidationResult> list = buildResults(response);
        ConversionReporter.setFhirLibraryValidationResults(list, matCqlLibrary.getId());


        list.forEach(v -> isValid(v, atomicBoolean));

        ConversionResult conversionResult = ConversionReporter.getConversionResult();
        conversionResult.findOrCreateLibraryConversionResults(matCqlLibrary.getId());

        response.setLibraryConversionResults(conversionResult.getLibraryConversionResults());
        response.setLibraryConversionType(conversionResult.getConversionType());

        return response;
    }

}
