package gov.cms.mat.fhir.services.service.orchestration;

import gov.cms.mat.cql.CqlParser;
import gov.cms.mat.cql.elements.BaseProperties;
import gov.cms.mat.cql.elements.IncludeProperties;
import gov.cms.mat.cql.elements.UsingProperties;
import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.rest.dto.CqlConversionResult;
import gov.cms.mat.fhir.rest.dto.FhirValidationResult;
import gov.cms.mat.fhir.rest.dto.LibraryConversionResults;
import gov.cms.mat.fhir.services.components.cql.CqlLibraryConverter;
import gov.cms.mat.fhir.services.components.library.UnConvertedCqlLibraryHandler;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.components.mongo.ConversionResult;
import gov.cms.mat.fhir.services.components.mongo.HapiResourcePersistedState;
import gov.cms.mat.fhir.services.config.LibraryConversionFileConfig;
import gov.cms.mat.fhir.services.exceptions.FhirLibraryNotFoundInMapException;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.rest.support.CqlVersionConverter;
import gov.cms.mat.fhir.services.rest.support.FhirValidatorProcessor;
import gov.cms.mat.fhir.services.service.CQLLibraryTranslationService;
import gov.cms.mat.fhir.services.service.CqlLibraryDataService;
import gov.cms.mat.fhir.services.service.support.ErrorSeverityChecker;
import gov.cms.mat.fhir.services.summary.CqlLibraryFindData;
import gov.cms.mat.fhir.services.summary.FhirLibraryResourceValidationResult;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import gov.cms.mat.fhir.services.translate.IdGenerator;
import gov.cms.mat.fhir.services.translate.MatLibraryTranslator;
import gov.cms.mat.fhir.services.translate.creators.FhirLibraryHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Library;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static gov.cms.mat.fhir.services.service.CQLLibraryTranslationService.ConversionType.FHIR;

@Component
@Slf4j
public class LibraryOrchestrationValidationService extends LibraryOrchestrationBase
        implements FhirValidatorProcessor, ErrorSeverityChecker, CqlVersionConverter, FhirLibraryHelper, IdGenerator {

    private static final String VALIDATION_FAILURE_MESSAGE = "Library validation failed";
    private static final String HAPI_FAILURE_MESSAGE = "Cannot find hapi fhir library with name: %s and version: %s";

    private final CqlLibraryDataService cqlLibraryDataService;
    private final CQLLibraryTranslationService cqlLibraryTranslationService;
    private final UnConvertedCqlLibraryHandler unConvertedCqlLibraryHandler;
    private final CqlLibraryConverter cqlLibraryConverter;
    private final LibraryConversionFileConfig libraryConversionFileConfig;

    public LibraryOrchestrationValidationService(HapiFhirServer hapiFhirServer,
                                                 CqlLibraryDataService cqlLibraryDataService,
                                                 CQLLibraryTranslationService cqlLibraryTranslationService,
                                                 UnConvertedCqlLibraryHandler unConvertedCqlLibraryHandler,
                                                 CqlLibraryConverter cqlLibraryConverter,
                                                 LibraryConversionFileConfig libraryConversionFileConfig) {
        super(hapiFhirServer);
        this.cqlLibraryDataService = cqlLibraryDataService;
        this.cqlLibraryTranslationService = cqlLibraryTranslationService;
        this.unConvertedCqlLibraryHandler = unConvertedCqlLibraryHandler;
        this.cqlLibraryConverter = cqlLibraryConverter;
        this.libraryConversionFileConfig = libraryConversionFileConfig;
    }

    public void processIncludedLibrary(IncludeProperties include, UsingProperties using, boolean showWarnings) {
        CqlLibraryFindData data = buildFindData(include, using);
        String unconvertedName = unConvertedCqlLibraryHandler.makeCqlName(data);

        String fhir4Name = include.getName() + BaseProperties.LIBRARY_FHIR_EXTENSION;
        String version = include.getVersion();

        var optional = findLibFile(libraryConversionFileConfig.getOrder(), fhir4Name);


        if (optional.isPresent()) {
            version = findVersion(optional.get(), version);
        }

        Bundle bundle = hapiFhirServer.fetchLibraryBundleByVersionAndName(version,
                include.getName() + BaseProperties.LIBRARY_FHIR_EXTENSION);

        if (CollectionUtils.isEmpty(bundle.getEntry())) {
            if (unConvertedCqlLibraryHandler.exists(data)) {
                log.info("Already exists in mongo for key: {}", unconvertedName);
            } else {
                CqlLibrary cqlLibrary = cqlLibraryDataService.findCqlLibrary(data);
                String cql =
                        cqlLibraryTranslationService.convertMatXmlToCql(cqlLibrary.getCqlXml(), null, showWarnings);
                unConvertedCqlLibraryHandler.write(data, cql);
            }
        } else {
            log.debug("Included Library already in fhir: {}", include);

            if (unConvertedCqlLibraryHandler.delete(data)) {
                log.info("Removed from mongo for key: {}", unconvertedName); // making progress
            }
        }
    }

    private CqlLibraryFindData buildFindData(IncludeProperties include, UsingProperties using) {
        return CqlLibraryFindData.builder()
                .qdmVersion(using.getVersion())
                .name(include.getName())
                .matVersion(convertVersionToBigDecimal(include.getVersion()))
                .version(include.getVersion())
                .type(using.isFhir() ? "FHIR" : "QDM")
                .build();
    }

    public void processIncludes(String cql, boolean showWarnings) {
        CqlParser cqlParser = new CqlParser(cql);
        UsingProperties using = cqlParser.getUsing();

        List<IncludeProperties> includes = cqlParser.getIncludes();

        includes.forEach(includeProperties -> processIncludedLibrary(includeProperties, using, showWarnings));
    }


    boolean validate(OrchestrationProperties properties) {

        translateCqlMatLibsToFhir(properties);

        return validateLibs(properties);
    }

    private boolean validateLibs(OrchestrationProperties properties) {
        AtomicBoolean atomicBoolean = new AtomicBoolean(true);

        properties.getCqlLibraries()
                .forEach(cqlLibrary -> convertQdmToFhir(cqlLibrary, properties.isShowWarnings()));

        // When no errors we would then convert to fhir and validate - for initial testing do for ALL
        properties.getCqlLibraries()
                .forEach(matLib -> findFhirLibraryAndValidate(properties, atomicBoolean, matLib));

        return atomicBoolean.get();
    }

    private void findFhirLibraryAndValidate(OrchestrationProperties properties,
                                            AtomicBoolean atomicBoolean,
                                            CqlLibrary matLib) {

        var optional = ConversionReporter.findFhirLibraryId(matLib.getId());

        if (optional.isPresent()) {
            validate(matLib, properties.findFhirLibrary(optional.get()), atomicBoolean);
        } else {
            throw new FhirLibraryNotFoundInMapException(matLib.getId());
        }
    }

    private void convertQdmToFhir(CqlLibrary matLib, boolean showWarnings) {
        String qdmCql = ConversionReporter.getCql(matLib.getId());
        String fhirCql = cqlLibraryConverter.convert(qdmCql);
        ConversionReporter.setFhirCql(fhirCql, matLib.getId());

        AtomicBoolean atomicBoolean = new AtomicBoolean(true);
        String fhirJson = cqlLibraryTranslationService.convertToJsonFromFhirCql(atomicBoolean, fhirCql, showWarnings);
        String cleanedJson = cleanJsonFromMatExceptions(fhirJson);
        ConversionReporter.setFhirJson(cleanedJson, matLib.getId());

        cqlLibraryTranslationService.processJsonForError(FHIR, fhirJson, matLib.getId());
    }

    private void validateQdm(CqlLibrary matLib, AtomicBoolean atomicBoolean, boolean showWarnings) {
        String cql = ConversionReporter.getCql(matLib.getId());

        String json = cqlLibraryTranslationService.convertToJson(matLib,
                atomicBoolean,
                cql,
                CQLLibraryTranslationService.ConversionType.QDM,
                showWarnings);

        String cleanedJson = cleanJsonFromMatExceptions(json);
        ConversionReporter.setElm(cleanedJson, matLib.getId());
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

        String cql = ConversionReporter.getCql(cqlLibrary.getId());
        String elm = ConversionReporter.getElm(cqlLibrary.getId());

        MatLibraryTranslator matLibraryTranslator = new MatLibraryTranslator(cqlLibrary,
                cql.getBytes(),
                elm.getBytes(),
                hapiFhirServer.getBaseURL(),
                createId());

        Library fhirLibrary = matLibraryTranslator.translateToFhir(null);

        ConversionReporter.setFhirLibraryId(fhirLibrary.getId(), cqlLibrary.getId());

        results.setFhirLibraryJson(hapiFhirServer.toJson(fhirLibrary));


        return fhirLibrary;
    }


    private FhirLibraryResourceValidationResult validate(CqlLibrary matCqlLibrary, Library fhirLibrary, AtomicBoolean atomicBoolean) {

        return validateFhirLibrary(matCqlLibrary.getId(), matCqlLibrary.getMeasureId(), fhirLibrary, atomicBoolean);

    }

    public FhirLibraryResourceValidationResult validateFhirLibrary(String matCqlLibraryId,
                                                                   String measureId,
                                                                   Library fhirLibrary,
                                                                   AtomicBoolean atomicBoolean) {
        FhirLibraryResourceValidationResult response = new FhirLibraryResourceValidationResult(matCqlLibraryId);
        response.setMeasureId(measureId);

        validateResource(response, fhirLibrary, hapiFhirServer.getCtx());

        List<FhirValidationResult> list = buildResults(response);
        ConversionReporter.setFhirLibraryValidationResults(list, matCqlLibraryId);

        if (list.isEmpty()) {
            ConversionReporter.setLibraryValidationLink(null, HapiResourcePersistedState.VALIDATION, matCqlLibraryId);
        } else {
            list.forEach(validationResult -> processValidation(validationResult, atomicBoolean, matCqlLibraryId));
        }

        ConversionResult conversionResult = ConversionReporter.getConversionResult();

        response.setLibraryConversionResults(conversionResult.getLibraryConversionResults());
        response.setLibraryConversionType(conversionResult.getConversionType());

        var optional = find(conversionResult.getLibraryConversionResults(), matCqlLibraryId);

        if (optional.isPresent()) {
            LibraryConversionResults results = optional.get();

            CqlConversionResult cqlConversionResult = results.getCqlConversionResult();

            if (!cqlConversionResult.getFhirCqlConversionErrors().isEmpty()) {
                ConversionReporter.setLibraryValidationError("Fhir Validation failed", matCqlLibraryId);
            }
        }

        return response;
    }


    private Optional<LibraryConversionResults> find(List<LibraryConversionResults> libraryConversionResults, String matLibId) {
        return libraryConversionResults.stream().filter(t -> t.getMatLibraryId().equals(matLibId)).findFirst();
    }


    private boolean processValidation(FhirValidationResult validationResult, AtomicBoolean atomicBoolean, String matLibId) {

        boolean valid = isValid(validationResult, atomicBoolean);

        if (valid) {
            ConversionReporter.setLibraryValidationLink(null, HapiResourcePersistedState.VALIDATION, matLibId);
        } else {
            ConversionReporter.setLibraryValidationError("Validation Failed", matLibId);
        }

        return valid;
    }
}
