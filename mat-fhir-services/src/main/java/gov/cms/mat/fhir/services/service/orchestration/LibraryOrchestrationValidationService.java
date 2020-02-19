package gov.cms.mat.fhir.services.service.orchestration;

import gov.cms.mat.cql.CqlParser;
import gov.cms.mat.cql.elements.IncludeProperties;
import gov.cms.mat.cql.elements.UsingProperties;
import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.rest.dto.FhirValidationResult;
import gov.cms.mat.fhir.rest.dto.LibraryConversionResults;
import gov.cms.mat.fhir.services.components.cql.CqlLibraryConverter;
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
import gov.cms.mat.fhir.services.translate.MatLibraryTranslator;
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
    private final CqlLibraryConverter cqlLibraryConverter;

    public LibraryOrchestrationValidationService(HapiFhirServer hapiFhirServer,
                                                 CqlLibraryDataService cqlLibraryDataService,
                                                 CQLLibraryTranslationService cqlLibraryTranslationService,
                                                 UnConvertedCqlLibraryFileHandler unConvertedCqlLibraryFileHandler, CqlLibraryConverter cqlLibraryConverter) {
        super(hapiFhirServer);
        this.cqlLibraryDataService = cqlLibraryDataService;
        this.cqlLibraryTranslationService = cqlLibraryTranslationService;
        this.unConvertedCqlLibraryFileHandler = unConvertedCqlLibraryFileHandler;
        this.cqlLibraryConverter = cqlLibraryConverter;
    }

    public void processIncludedLibrary(IncludeProperties include, UsingProperties using) {

        Bundle bundle = hapiFhirServer.fetchLibraryBundleByVersionAndName(include.getVersion(), include.getName());

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
        List<IncludeProperties> includes = cqlParser.getIncludes();
        UsingProperties using = cqlParser.getUsing();

        // when no more includes recursion will stop
        includes.forEach(includeProperties -> processIncludedLibrary(includeProperties, using));
    }

    boolean validate(OrchestrationProperties properties) {

        translateCqlMatLibsToFhir(properties);

        return validateLibs(properties);
    }

    private boolean validateLibs(OrchestrationProperties properties) {
        AtomicBoolean atomicBoolean = new AtomicBoolean(true);

        properties.getCqlLibraries()
                .forEach(matLib -> validateQdm(matLib, atomicBoolean));

        if (!atomicBoolean.get()) {
            //log.warn("IGNORED FOR TESTING: Terminal message errorMessage: {},  ConversionOutcome:{}",
            //         VALIDATION_FAILURE_MESSAGE, LIBRARY_VALIDATION_FAILED);
            ConversionReporter.setTerminalMessage(VALIDATION_FAILURE_MESSAGE, LIBRARY_VALIDATION_FAILED);
        }

        properties.getCqlLibraries().forEach(this::convertQdm);

        // When no errors we would then convert to fhir and validate - for initial testing do for ALL

        properties.getCqlLibraries()
                .forEach(matLib -> validate(matLib, properties.findFhirLibrary(matLib.getId()), atomicBoolean));

        return atomicBoolean.get();
    }

    private void convertQdm(CqlLibrary matLib) {
        String qdmCql = ConversionReporter.getCql(matLib.getId());
        String fhirCql = cqlLibraryConverter.convert(qdmCql);
        ConversionReporter.setFhirCql(fhirCql, matLib.getId());

        AtomicBoolean atomicBoolean = new AtomicBoolean(true);
        String fhirJson = cqlLibraryTranslationService.convertToJsonFromFhirCql(atomicBoolean, fhirCql);
        ConversionReporter.setFhirJson(fhirJson, matLib.getId());
    }

    private void validateQdm(CqlLibrary matLib, AtomicBoolean atomicBoolean) {
        String cql = ConversionReporter.getCql(matLib.getId());
        String json = cqlLibraryTranslationService.convertToJson(matLib,
                atomicBoolean,
                cql,
                CQLLibraryTranslationService.ConversionType.QDM);

        ConversionReporter.setElm(json, matLib.getId());
    }

    private void translateCqlMatLibsToFhir(OrchestrationProperties properties) {
        List<Library> libraryList = properties.getCqlLibraries().stream()
                .map(this::translateCqlLib)
                .collect(Collectors.toList());

        properties.getFhirLibraries().addAll(libraryList);
    }

    private Library translateCqlLib(CqlLibrary cqlLibrary) {
        ConversionResult conversionResult = ConversionReporter.getConversionResult();
        LibraryConversionResults results = conversionResult.findLibraryConversionResultsRequired(cqlLibrary.getId());

        MatLibraryTranslator matLibraryTranslator = new MatLibraryTranslator(cqlLibrary,
                results.getCqlConversionResult().getCql().getBytes(),
                results.getCqlConversionResult().getElm().getBytes(),
                hapiFhirServer.getBaseURL());

        Library fhirLibrary = matLibraryTranslator.translateToFhir(null);

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

        list.forEach(validationResult -> isValid(validationResult, atomicBoolean));

        ConversionResult conversionResult = ConversionReporter.getConversionResult();

        response.setLibraryConversionResults(conversionResult.getLibraryConversionResults());
        response.setLibraryConversionType(conversionResult.getConversionType());

        return response;
    }
}
