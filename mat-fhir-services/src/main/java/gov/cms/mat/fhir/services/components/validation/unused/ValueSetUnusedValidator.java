package gov.cms.mat.fhir.services.components.validation.unused;

import mat.model.cql.CQLModel;
import mat.model.cql.CQLQualityDataSetDTO;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


public class ValueSetUnusedValidator extends UnusedValidatorBase<CQLQualityDataSetDTO> {
    ValueSetUnusedValidator(CQLModel cqlModel) {
        super(cqlModel);
    }

    @Override
    public List<CQLQualityDataSetDTO> findUnused() {
        if (CollectionUtils.isEmpty(cqlModel.getValueSetList())) {
            return Collections.emptyList();
        } else {
            return cqlModel.getValueSetList()
                    .stream()
                    .filter(this::isNotUsedInCql)
                    .collect(Collectors.toList());
        }
    }

    @Override
    String findTarget(CQLQualityDataSetDTO valueSet) {
        return '"' + valueSet.getCodeListName() + '"';
    }
}
