package gov.cms.mat.fhir.services.service.orchestration;

import gov.cms.mat.cql.dto.CqlConversionPayload;
import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.rest.dto.CqlConversionResult;
import gov.cms.mat.fhir.rest.dto.FhirValidationResult;
import gov.cms.mat.fhir.rest.dto.LibraryConversionResults;
import gov.cms.mat.fhir.services.components.cql.CqlLibraryConverter;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.components.mongo.ConversionResult;
import gov.cms.mat.fhir.services.components.mongo.HapiResourcePersistedState;
import gov.cms.mat.fhir.services.exceptions.FhirLibraryNotFoundInMapException;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.rest.support.CqlVersionConverter;
import gov.cms.mat.fhir.services.rest.support.FhirValidatorProcessor;
import gov.cms.mat.fhir.services.service.CQLLibraryTranslationService;
import gov.cms.mat.fhir.services.service.support.ErrorSeverityChecker;
import gov.cms.mat.fhir.services.summary.FhirLibraryResourceValidationResult;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import gov.cms.mat.fhir.services.translate.IdGenerator;
import gov.cms.mat.fhir.services.translate.LibraryTranslator;
import gov.cms.mat.fhir.services.translate.creators.FhirCreator;
import gov.cms.mat.fhir.services.translate.creators.FhirLibraryHelper;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Library;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static gov.cms.mat.fhir.services.service.CQLLibraryTranslationService.ConversionType.FHIR;

@Component
@Slf4j
public class LibraryOrchestrationValidationService extends LibraryOrchestrationBase
        implements FhirValidatorProcessor,
        ErrorSeverityChecker,
        CqlVersionConverter,
        FhirLibraryHelper,
        IdGenerator,
        FhirCreator {

    private final CQLLibraryTranslationService cqlLibraryTranslationService;
    private final CqlLibraryConverter cqlLibraryConverter;
    private final LibraryTranslator libTranslator;

    public LibraryOrchestrationValidationService(HapiFhirServer hapiFhirServer,
                                                 CQLLibraryTranslationService cqlLibraryTranslationService,
                                                 CqlLibraryConverter cqlLibraryConverter,
                                                 LibraryTranslator libTranslator) {

        super(hapiFhirServer);
        this.cqlLibraryTranslationService = cqlLibraryTranslationService;
        this.cqlLibraryConverter = cqlLibraryConverter;
        this.libTranslator = libTranslator;
    }


    boolean validate(OrchestrationProperties properties) {
        convertQdmToFhir(properties.getMeasureLib(), properties.isShowWarnings(), properties.isIncludeStdLibs());
        translateCqlMatLibsToFhir(properties);
        return validateLibs(properties);
    }

    private boolean validateLibs(OrchestrationProperties properties) {
        AtomicBoolean atomicBoolean = new AtomicBoolean(true);

        // When no errors we would then convert to fhir and validate - for initial testing do for ALL
        findFhirLibraryAndValidate(properties, atomicBoolean, properties.getMeasureLib());

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

    private void convertQdmToFhir(CqlLibrary matLib, boolean showWarnings, boolean includeStdLibs) {
        String qdmCql = ConversionReporter.getCql(matLib.getId());
        String fhirCql = cqlLibraryConverter.convert(qdmCql, includeStdLibs);
        ConversionReporter.setFhirCql(fhirCql, matLib.getId());

        AtomicBoolean atomicBoolean = new AtomicBoolean(true);
        CqlConversionPayload fhirJson = cqlLibraryTranslationService.convertToJsonFromFhirCql(atomicBoolean, fhirCql, showWarnings);
        String cleanedJson = cleanJsonFromMatExceptions(fhirJson.getJson());
        ConversionReporter.setFhirElmJson(cleanedJson, matLib.getId());
        ConversionReporter.setFhirElmXml(fhirJson.getXml(), matLib.getId());

        cqlLibraryTranslationService.processJsonForError(FHIR, fhirJson.getJson(), matLib.getId());
    }


    private void translateCqlMatLibsToFhir(OrchestrationProperties properties) {
        Library library = translateCqlLib(properties.getMeasureLib());
        properties.getFhirLibraries().add(library);
    }

    private Library translateCqlLib(CqlLibrary cqlLibrary) {
        ConversionResult conversionResult = ConversionReporter.getConversionResult();
        LibraryConversionResults results = conversionResult.findLibraryConversionResultsRequired(cqlLibrary.getId());

        String fhirCql = ConversionReporter.getFhirCql(cqlLibrary.getId());
        String fhirElm = ConversionReporter.getFhirElmJson(cqlLibrary.getId());
        String fhirXml = ConversionReporter.getFhirElmXml(cqlLibrary.getId());

        Library lib = libTranslator.translateToFhir(cqlLibrary.getId(),
                fhirCql,
                fhirElm,
                fhirXml);

        ConversionReporter.setFhirLibraryId(lib.getId(), cqlLibrary.getId());
        results.setFhirLibraryJson(hapiFhirServer.toJson(lib));

        return lib;
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
            ConversionReporter.setLibraryValidationLink(fhirLibrary.getUrl(), HapiResourcePersistedState.VALIDATION, matCqlLibraryId);
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
                ConversionReporter.setLibraryValidationError(fhirLibrary.getUrl(), "Fhir Validation failed", matCqlLibraryId);
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
            ConversionReporter.setLibraryValidationError(null, "Validation Failed", matLibId);
        }

        return valid;
    }
}
