package gov.cms.mat.fhir.services.components.validation;

import gov.cms.mat.cql.CqlTextParser;
import gov.cms.mat.fhir.services.rest.dto.LibraryErrors;
import lombok.extern.slf4j.Slf4j;
import mat.model.cql.CQLQualityDataSetDTO;
import mat.shared.CQLError;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ValueSetValidator extends CqlValidatorHelper {
    private final VsacValueSetValidator vsacValueSetValidator;

    public ValueSetValidator(VsacValueSetValidator vsacValueSetValidator) {
        this.vsacValueSetValidator = vsacValueSetValidator;
    }

    @Async("threadPoolValidation")
    public CompletableFuture<List<LibraryErrors>> validate(long timeout,
                                                           List<CQLQualityDataSetDTO> valueSetList,
                                                           String cql,
                                                           String umlsToken) {
        List<CQLQualityDataSetDTO> failingValueSets = vsacValueSetValidator.validate(timeout, valueSetList, umlsToken);

        if (failingValueSets.isEmpty()) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        } else {
            CqlTextParser cqlTextParser = new CqlTextParser(cql);
            LibraryErrors libraryErrors = buildLibraryErrors(cqlTextParser);

            List<CQLError> cqlErrors = failingValueSets.stream()
                    .map(c -> findLine(c.getOid(), c.getErrorMessage(), cqlTextParser.getLines()))
                    .collect(Collectors.toList());

            libraryErrors.setErrors(cqlErrors);

            return CompletableFuture.completedFuture(List.of(libraryErrors));
        }
    }

    private CQLError findLine(String oid, String errorMessage, String[] lines) {
        int lineCounter = 1;
        int lineIndex = -1;
        int lineLength = -1;

        //filter by comment & todo antlr

        for (String cqlLine : lines) {
            if (cqlLine.contains(oid)) {
                lineLength = cqlLine.length();
                lineIndex = lineCounter;
                break;
            } else {
                lineCounter++;
            }
        }

        return createCqlError(errorMessage, lineIndex, lineLength);
    }
}
