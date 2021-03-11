package gov.cms.mat.fhir.services.components.validation.unused;

import mat.model.cql.CQLModel;
import org.springframework.util.CollectionUtils;

import java.util.List;

abstract class UnusedValidatorBase<T> {
    final CQLModel cqlModel;

    UnusedValidatorBase(CQLModel cqlModel) {
        this.cqlModel = cqlModel;
    }

    public abstract List<T> findUnused();

    abstract String findTarget(T type);

    boolean isNotUsedInCql(T type) {
        String target = findTarget(type);
        return isNotUsedInFunctions(target) && isNotUsedInDefinitions(target);
    }

    boolean isNotUsedInFunctions(String target) {
        if (CollectionUtils.isEmpty(cqlModel.getCqlFunctions())) {
            return true;
        } else {
            return cqlModel.getCqlFunctions().stream()
                    .noneMatch(d -> d.getFunctionLogic().contains(target));
        }
    }

    boolean isNotUsedInDefinitions(String target) {
        if (CollectionUtils.isEmpty(cqlModel.getDefinitionList())) {
            return true;
        } else {
            return cqlModel.getDefinitionList().stream()
                    .noneMatch(d -> d.getDefinitionLogic().contains(target));
        }
    }
}
