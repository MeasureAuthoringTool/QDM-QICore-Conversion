package gov.cms.mat.fhir.services.components.validation;

import gov.cms.mat.cql.CqlTextParser;
import gov.cms.mat.fhir.services.rest.dto.LibraryErrors;
import lombok.extern.slf4j.Slf4j;
import mat.model.cql.CQLCode;
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
    private final VsacCodeSystemValidator vsacCodeSystemValidator;

    public CodeSystemValidator(VsacCodeSystemValidator vsacCodeSystemValidator) {
        this.vsacCodeSystemValidator = vsacCodeSystemValidator;
    }

    @Async("threadPoolValidation")
    public CompletableFuture<List<LibraryErrors>> validate(long timeout,
                                                           List<CQLCode> valueSetList,
                                                           String cql,
                                                           String umlsToken) {
        List<CQLCode> failingCodes = vsacCodeSystemValidator.validate(timeout, valueSetList, umlsToken);

        if (failingCodes.isEmpty()) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        } else {
            CqlTextParser cqlTextParser = new CqlTextParser(cql);
            LibraryErrors libraryErrors = buildLibraryErrors(cqlTextParser);

            List<CQLError> cqlErrors = failingCodes.stream()
                    .map(c -> findLine(c.getCodeSystemOID(), cqlTextParser.getLines()))
                    .collect(Collectors.toList());

            libraryErrors.setErrors(cqlErrors);

            return CompletableFuture.completedFuture(List.of(libraryErrors));
        }
    }

    @Override
    public String getType() {
        return "CodeSystem";
    }
}