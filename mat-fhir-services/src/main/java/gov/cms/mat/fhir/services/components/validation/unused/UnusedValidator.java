package gov.cms.mat.fhir.services.components.validation.unused;

import gov.cms.mat.fhir.services.rest.dto.UnusedCqlElements;
import mat.model.cql.CQLModel;

public class UnusedValidator {
    private final CQLModel cqlModel;

    public UnusedValidator(CQLModel cqlModel) {
        this.cqlModel = cqlModel;
    }

    public UnusedCqlElements findUnused() {
        return UnusedCqlElements.builder()
                .valueSets(new ValueSetUnusedValidator(cqlModel).findUnused())
                .libraries(new LibraryUnusedValidator(cqlModel).findUnused())
                .codes(new CodeUnusedValidator(cqlModel).findUnused())
                .build();
    }
}
