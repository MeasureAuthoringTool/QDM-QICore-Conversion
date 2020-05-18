package gov.cms.mat.fhir.services.service.packaging;

import gov.cms.mat.fhir.rest.dto.FhirIncludeLibraryReferences;
import gov.cms.mat.fhir.rest.dto.FhirIncludeLibraryResult;
import gov.cms.mat.fhir.services.components.fhir.FhirIncludeLibraryProcessor;
import gov.cms.mat.fhir.services.translate.creators.FhirLibraryHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.hl7.fhir.r4.model.Library;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class LibraryIncludeProcessor implements FhirLibraryHelper {
    private final FhirIncludeLibraryProcessor fhirIncludeLibraryProcessor;

    public LibraryIncludeProcessor(FhirIncludeLibraryProcessor fhirIncludeLibraryProcessor) {
        this.fhirIncludeLibraryProcessor = fhirIncludeLibraryProcessor;
    }

    FhirIncludeLibraryResult process(Library library) {
        FhirIncludeLibraryResult result = findIncludedFhirLibraries(library);

        if (CollectionUtils.isEmpty(result.getLibraryReferences())) {
            return result;
        } else {
            return processIncludes(result);
        }
    }

    private FhirIncludeLibraryResult processIncludes(FhirIncludeLibraryResult result) {
        while (isAllNotScanned(result.getLibraryReferences())) {
            List<FhirIncludeLibraryReferences> includes = new ArrayList<>();

            for (FhirIncludeLibraryReferences r : result.getLibraryReferences()) {
                if (!r.isScannedForIncludedLibraries()) {
                    includes.addAll(processInclude(r, result));
                }
            }

            result.getLibraryReferences().addAll(includes);
        }

        return result;
    }

    private List<FhirIncludeLibraryReferences> processInclude(FhirIncludeLibraryReferences libraryReference,
                                                              FhirIncludeLibraryResult parentResult) {
        log.debug("Finding includes for libraryReference {}-{}",
                libraryReference.getName(), libraryReference.getVersion());

        libraryReference.setScannedForIncludedLibraries(true);
        Library library = libraryReference.getLibrary();

        FhirIncludeLibraryResult childResult = findIncludedFhirLibraries(library);

        if (CollectionUtils.isEmpty(childResult.getLibraryReferences())) {
            log.debug("Library {}-{} does not contain any includes",
                    library.getName(), library.getVersion());
            return Collections.emptyList();
        } else {
            log.debug("Library {}-{} contains  {} includes",
                    library.getName(), library.getVersion(), childResult.getLibraryReferences());

            return childResult.getLibraryReferences().stream()
                    .filter(r -> !parentResult.getLibraryReferences().contains(r))
                    .collect(Collectors.toList());
        }
    }

    private boolean isAllNotScanned(List<FhirIncludeLibraryReferences> libraryReferences) {
        return libraryReferences.stream()
                .anyMatch(r -> !r.isScannedForIncludedLibraries());
    }

    private FhirIncludeLibraryResult findIncludedFhirLibraries(Library library) {
        return findIncludedLibraries(library, fhirIncludeLibraryProcessor);
    }
}
