package gov.cms.mat.fhir.services.service;


import gov.cms.mat.cql.dto.CqlConversionPayload;
import gov.cms.mat.fhir.rest.dto.CqlConversionError;
import gov.cms.mat.fhir.services.rest.dto.LibraryErrors;
import gov.cms.mat.fhir.services.service.support.ElmErrorExtractor;
import lombok.extern.slf4j.Slf4j;
import mat.shared.CQLError;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class ValidationService {
    private final CQLLibraryTranslationService cqlLibraryTranslationService;

    public ValidationService(CQLLibraryTranslationService cqlLibraryTranslationService) {
        this.cqlLibraryTranslationService = cqlLibraryTranslationService;
    }

    @Async("threadPoolValidation")
    public CompletableFuture<List<LibraryErrors>> validateCql(String cql) {
        log.info("Running async validateCql");

        CqlConversionPayload payload = cqlLibraryTranslationService.convertCqlToJson(cql, false);

        ElmErrorExtractor extractor = new ElmErrorExtractor(payload.getJson());

        List<CqlConversionError> cqlConversionErrors = extractor.parseForAnnotations();

        List<LibraryErrors> libraryErrors = new ArrayList<>();

        cqlConversionErrors.forEach(c -> process(c, libraryErrors));

        Map<String, List<CqlConversionError>> map = extractor.parseForExternalErrors();

        return CompletableFuture.completedFuture(libraryErrors);
    }

    private void process(CqlConversionError cqlConversionError, List<LibraryErrors> libraryErrors) {
        LibraryErrors l = findOrCreate(cqlConversionError, libraryErrors);
        l.getErrors().add(convert(cqlConversionError));
    }

    private CQLError convert(CqlConversionError cqlConversionError) {
        CQLError cqlError = new CQLError();
        cqlError.setSeverity(cqlConversionError.getErrorSeverity());
        cqlError.setErrorMessage(cqlConversionError.getMessage());

        cqlError.setStartErrorInLine(cqlConversionError.getStartLine());
        cqlError.setErrorInLine(cqlConversionError.getStartLine());
        cqlError.setErrorAtOffset(cqlConversionError.getStartChar());
        cqlError.setStartErrorAtOffset(cqlConversionError.getStartChar());
        cqlError.setEndErrorInLine(cqlConversionError.getEndLine());
        cqlError.setErrorAtOffset(cqlConversionError.getEndChar());

        return cqlError;
    }

    private LibraryErrors findOrCreate(CqlConversionError cqlError, List<LibraryErrors> libraryErrors) {
        var optional = libraryErrors.stream()
                .filter(l -> isSame(l, cqlError))
                .findFirst();

        return optional
                .orElseGet(() -> createAndAddToList(cqlError, libraryErrors));
    }

    private LibraryErrors createAndAddToList(CqlConversionError cqlError, List<LibraryErrors> libraryErrors) {
        LibraryErrors libraryError = new LibraryErrors(cqlError.getLibraryId(), cqlError.getTargetIncludeLibraryVersionId());

        libraryErrors.add(libraryError);

        return libraryError;
    }

    private boolean isSame(LibraryErrors l, CqlConversionError cqlConversionError) {
        return l.getName().equals(cqlConversionError.getTargetIncludeLibraryId()) &&
                l.getVersion().equals(cqlConversionError.getLibraryVersion());

    }
}
