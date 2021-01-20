package gov.cms.mat.fhir.services.components.validation;

import gov.cms.mat.cql.CqlTextParser;
import gov.cms.mat.fhir.services.rest.dto.LibraryErrors;
import lombok.extern.slf4j.Slf4j;
import mat.model.cql.CQLCode;
import mat.model.cql.CQLCodeSystem;
import mat.shared.CQLError;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CodeSystemValidator extends CqlValidatorHelper {
    private static final String CODE_SYSTEM_NOT_REFERENCED = "Code System is not referenced.";
    private static final String CODE_ERRORS = "802"; // VSAC error when cannot find code
    private final VsacCodeSystemValidator vsacCodeSystemValidator;

    public CodeSystemValidator(VsacCodeSystemValidator vsacCodeSystemValidator) {
        this.vsacCodeSystemValidator = vsacCodeSystemValidator;
    }

    @Async("threadPoolValidation")
    public CompletableFuture<List<LibraryErrors>> validate(long timeout,
                                                           List<CQLCode> codeList,
                                                           List<CQLCodeSystem> codeSystemList,
                                                           String cql,
                                                           String umlsToken,
                                                           String apiKey) {
        List<CQLCode> failingCodes = vsacCodeSystemValidator.validate(timeout, codeList, umlsToken, apiKey);
        List<CQLError> unReferencedCodeSystems = findUnreferencedCodeSystems(codeSystemList, codeList);

        if (failingCodes.isEmpty()) {
            if( unReferencedCodeSystems.isEmpty()) {
                return CompletableFuture.completedFuture(Collections.emptyList());
            } else {
                CqlTextParser cqlTextParser = new CqlTextParser(cql);
                LibraryErrors libraryErrors = buildLibraryErrors(cqlTextParser);
                libraryErrors.getErrors().addAll(unReferencedCodeSystems);
                return CompletableFuture.completedFuture(List.of(libraryErrors));
            }
        } else {
            CqlTextParser cqlTextParser = new CqlTextParser(cql);
            LibraryErrors libraryErrors = buildLibraryErrors(cqlTextParser);

            List<CQLError> codeSystemErrors = failingCodes.stream()
                    .filter(c -> c.getErrorCode() != null && !c.getErrorCode().equals(CODE_ERRORS))
                    .map(c -> createCodeSystemError(c, codeSystemList))
                    .collect(Collectors.toList());
            libraryErrors.getErrors().addAll(codeSystemErrors);

            libraryErrors.getErrors().addAll(unReferencedCodeSystems);

            List<CQLError> codeErrors = failingCodes.stream()
                    .filter(c -> c.getErrorCode() == null || c.getErrorCode().equals(CODE_ERRORS))
                    .map(this::createCodeError)
                    .collect(Collectors.toList());

            libraryErrors.getErrors().addAll(codeErrors);

            return CompletableFuture.completedFuture(List.of(libraryErrors));
        }
    }

    private List<CQLError> findUnreferencedCodeSystems(List<CQLCodeSystem> codeSystemList, List<CQLCode> codeList) {
        List<CQLCodeSystem> notReferences = codeSystemList.stream()
                .filter(codeSystem -> codeSystemNotReferenced(codeSystem, codeList))
                .collect(Collectors.toList());

        if (notReferences.isEmpty()) {
            return Collections.emptyList();
        } else {
            return notReferences.stream()
                    .map(c -> createCqlError(CODE_SYSTEM_NOT_REFERENCED, c.getLineNumber(), 25))
                    .collect(Collectors.toList());
        }

    }

    private boolean codeSystemNotReferenced(CQLCodeSystem cqlCodeSystem, List<CQLCode> codeList) {
        return codeList.stream()
                .noneMatch(cqlCode -> cqlCode.getCodeSystemName().equals(cqlCodeSystem.getCodeSystemName()));
    }

    private CQLError createCodeSystemError(CQLCode cqlCode, List<CQLCodeSystem> codeSystemList) {

        var optionalCQLCodeSystem = codeSystemList.stream()
                .filter(c -> c.getCodeSystemName().equals(cqlCode.getCodeSystemName()))
                .findFirst();

        int lineNumber;

        if (optionalCQLCodeSystem.isPresent()) {
            lineNumber = optionalCQLCodeSystem.get().getLineNumber();
        } else { // should never happen
            log.warn("Cannot find a code system for CodeSystemName: {} ", cqlCode.getCodeSystemName());
            lineNumber = cqlCode.getLineNumber();
        }

        String code = "codesystem " + cqlCode.getCodeSystemName() + ": " + cqlCode.getCodeIdentifier();
        return createCqlError(cqlCode.getErrorMessage(), lineNumber, code.length());
    }


    private CQLError createCodeError(CQLCode cqlCode) {
        String code = "codesystem " + cqlCode.getCodeSystemName() + ": " + cqlCode.getCodeIdentifier();
        return createCqlError(cqlCode.getErrorMessage(), cqlCode.getLineNumber(), code.length());
    }
}
