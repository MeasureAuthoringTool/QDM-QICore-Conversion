package gov.cms.mat.fhir.services.service.orchestration;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.rest.dto.FhirValidationResult;
import gov.cms.mat.fhir.rest.dto.LibraryConversionResults;
import gov.cms.mat.fhir.services.components.cql.CqlParser;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.components.mongo.ConversionResult;
import gov.cms.mat.fhir.services.exceptions.CqlConversionException;
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

import static gov.cms.mat.fhir.rest.dto.ConversionOutcome.LIBRARY_VALIDATION_FAILED;

@Component
@Slf4j
public class LibraryOrchestrationValidationService extends LibraryOrchestrationBase
        implements FhirValidatorProcessor, ErrorSeverityChecker, CqlVersionConverter {

    private static final String FAILURE_MESSAGE = "Library validation failed";

    private final CqlLibraryDataService cqlLibraryDataService;
    private final CQLLibraryTranslationService cqlLibraryTranslationService;

    public LibraryOrchestrationValidationService(HapiFhirServer hapiFhirServer,
                                                 CqlLibraryDataService cqlLibraryDataService,
                                                 CQLLibraryTranslationService cqlLibraryTranslationService) {
        super(hapiFhirServer);
        this.cqlLibraryDataService = cqlLibraryDataService;
        this.cqlLibraryTranslationService = cqlLibraryTranslationService;
    }

    public void processIncludedLibrary(CqlParser.IncludeProperties include, CqlParser.UsingProperties using) {
        AtomicBoolean atomicBoolean = new AtomicBoolean(Boolean.TRUE);

        Bundle bundle = hapiFhirServer.getLibraryBundleByNameAndVersion(include.getVersion(), include.getName());

        if (CollectionUtils.isEmpty(bundle.getEntry())) {
            CqlLibraryFindData data = CqlLibraryFindData.builder()
                    .qdmVersion(using.getVersion())
                    .name(include.getName())
                    .matVersion(convertVersionToBigDecimal(include.getVersion()))
                    .version(include.getVersion())
                    .build();

            CqlLibrary cqlLibrary = cqlLibraryDataService.findCqlLibrary(data);

            String cql = cqlLibraryTranslationService.convertMatXmlToCql(cqlLibrary.getCqlXml(), null);

            ConversionReporter.setCql(cql, cqlLibrary.getCqlName(), cqlLibrary.getVersion(), cqlLibrary.getId());

            String json = cqlLibraryTranslationService.convertToJson(cqlLibrary, atomicBoolean, cql);
            ConversionReporter.setElm(json, cqlLibrary.getId());

            if (!atomicBoolean.get()) {
                log.debug("cgl : {}", cql);
                log.debug("json : {}", json);
                atomicBoolean.set(Boolean.TRUE);
                // throw new CqlConversionException("Cannot convert {}-{} cql to json");
            }

            LibraryTranslator libraryTranslator = new LibraryTranslator(cqlLibrary,
                    cql.getBytes(),
                    json.getBytes(),
                    hapiFhirServer.getBaseURL());

            Library fhirLibrary = libraryTranslator.translateToFhir(include.getVersion());

            validate(cqlLibrary, fhirLibrary, atomicBoolean);

            if (!atomicBoolean.get()) {
                atomicBoolean.set(Boolean.TRUE);
                //throw new CqlConversionException("oops");
            }

            processPersisting(cqlLibrary, fhirLibrary, atomicBoolean);

            if (!atomicBoolean.get()) {
                throw new CqlConversionException("Error persisting  ");
            }

            processIncludes(cql); // recursive call

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
            ConversionReporter.setTerminalMessage(FAILURE_MESSAGE, LIBRARY_VALIDATION_FAILED);
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
