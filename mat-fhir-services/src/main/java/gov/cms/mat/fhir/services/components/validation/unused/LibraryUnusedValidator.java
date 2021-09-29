package gov.cms.mat.fhir.services.components.validation.unused;

import mat.model.cql.CQLIncludeLibrary;
import mat.model.cql.CQLModel;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class LibraryUnusedValidator extends UnusedValidatorBase<CQLIncludeLibrary> {
    public LibraryUnusedValidator(CQLModel cqlModel) {
        super(cqlModel);
    }

    @Override
    public List<CQLIncludeLibrary> findUnused() {
        if (CollectionUtils.isEmpty(cqlModel.getCqlIncludeLibrarys())) {
            return Collections.emptyList();
        } else {
            return cqlModel.getCqlIncludeLibrarys()
                    .stream()
                    //Ignore FHIRHelpers when determining unused list. It is needed for under-the-hood processing by the Translator.
                    .filter(l -> !l.getCqlLibraryName().equals("FHIRHelpers"))
                    .filter(this::isNotUsedInCql)
                    .collect(Collectors.toList());
        }
    }

    @Override
    String findTarget(CQLIncludeLibrary library) {
       return library.getAliasName() + '.';
    }
}
