package gov.cms.mat.fhir.services.components.validation.unused;

import mat.model.cql.CQLCode;
import mat.model.cql.CQLModel;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CodeUnusedValidator extends UnusedValidatorBase<CQLCode> {
    CodeUnusedValidator(CQLModel cqlModel) {
        super(cqlModel);
    }

    @Override
    public List<CQLCode> findUnused() {
        if (CollectionUtils.isEmpty(cqlModel.getCodeList())) {
            return Collections.emptyList();
        } else {
            return cqlModel.getCodeList()
                    .stream()
                    .filter(this::isNotUsedInCql)
                    .collect(Collectors.toList());
        }
    }

    @Override
    String findTarget(CQLCode cqlCode) {
        return '"' + cqlCode.getCodeName() + '"';
    }
}
