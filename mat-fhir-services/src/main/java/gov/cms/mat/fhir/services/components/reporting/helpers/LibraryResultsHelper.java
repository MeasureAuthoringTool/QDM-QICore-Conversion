package gov.cms.mat.fhir.services.components.reporting.helpers;

import gov.cms.mat.fhir.rest.dto.LibraryConversionResults;
import gov.cms.mat.fhir.services.exceptions.CqlLibraryNotFoundException;

import java.util.List;
import java.util.Optional;

public interface LibraryResultsHelper {
    List<LibraryConversionResults> getLibraryConversionResults();

    default Optional<LibraryConversionResults> findFirstLibraryResult(String matLibraryId) {
        return getLibraryConversionResults().stream()
                .filter(result -> result.getMatLibraryId() != null && result.getMatLibraryId().equals(matLibraryId))
                .findFirst();
    }

    default LibraryConversionResults createLibraryResult(String matLibraryId) {
        LibraryConversionResults createdResults = new LibraryConversionResults(matLibraryId);

        getLibraryConversionResults().add(createdResults);

        return createdResults;
    }

    default LibraryConversionResults findLibraryConversionResultsRequired(String matLibraryId) {
        return findFirstLibraryResult(matLibraryId)
                .orElseThrow(() -> new CqlLibraryNotFoundException(matLibraryId));
    }

    default LibraryConversionResults findOrCreateLibraryConversionResults(String matLibraryId) {
        return findFirstLibraryResult(matLibraryId)
                .orElseGet(() -> createLibraryResult(matLibraryId));
    }
}
