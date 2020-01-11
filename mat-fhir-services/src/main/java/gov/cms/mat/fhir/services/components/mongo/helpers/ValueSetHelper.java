package gov.cms.mat.fhir.services.components.mongo.helpers;

import gov.cms.mat.fhir.rest.dto.ValueSetConversionResults;
import gov.cms.mat.fhir.services.exceptions.ValueSetNotFoundException;

import java.util.List;
import java.util.Optional;

public interface ValueSetHelper {
    List<ValueSetConversionResults> getValueSetConversionResults();

    default Optional<ValueSetConversionResults> findFirstValueSetResult(String oid) {
        return getValueSetConversionResults().stream()
                .filter(l -> l.getOid().equals(oid))
                .findFirst();
    }

    default ValueSetConversionResults createValueSetResult(String oid) {
        ValueSetConversionResults createdResults = new ValueSetConversionResults(oid);

        getValueSetConversionResults().add(createdResults);

        return createdResults;
    }

    default ValueSetConversionResults findOrCreateValueSetConversionResults(String oid) {
        return findFirstValueSetResult(oid)
                .orElseGet(() -> createValueSetResult(oid));
    }

    default ValueSetConversionResults findValueSetConversionResultsRequired(String oid) {
        return findFirstValueSetResult(oid)
                .orElseThrow(() -> new ValueSetNotFoundException(oid));
    }
}
