package gov.cms.mat.fhir.services.service.packaging;

import gov.cms.mat.fhir.rest.dto.FhirIncludeLibraryReferences;
import gov.cms.mat.fhir.rest.dto.FhirIncludeLibraryResult;
import gov.cms.mat.fhir.services.ResourceFileUtil;
import gov.cms.mat.fhir.services.components.fhir.FhirIncludeLibraryProcessor;
import gov.cms.mat.fhir.services.exceptions.cql.LibraryAttachmentNotFoundException;
import org.apache.commons.collections4.CollectionUtils;
import org.hl7.fhir.r4.model.Library;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LibraryIncludeProcessorTest implements ResourceFileUtil, LibraryHelper {
    private Library qiCorePatternLibrary;
    private String qiCorePatternCql;

    private Library fhirHelpersLibrary;
    private String fhirHelpersCql;

    private Library matGlobalCommonFunctionsFhir4Library;
    private String matGlobalCommonFunctionsFhir4Cql;

    private Library scratchLibrary;
    private String scratchCql;

    @Mock
    private FhirIncludeLibraryProcessor fhirIncludeLibraryProcessor;
    @InjectMocks
    private LibraryIncludeProcessor libraryIncludeProcessor;

    @BeforeEach
    void setUp() {
        qiCorePatternCql = getStringFromResource("/includes/QICorePatterns.cql");
        qiCorePatternLibrary = createLib(qiCorePatternCql);

        fhirHelpersCql = getStringFromResource("/includes/FHIRHelpers.cql");
        fhirHelpersLibrary = createLib(fhirHelpersCql);

        matGlobalCommonFunctionsFhir4Cql = getStringFromResource("/includes/MATGlobalCommonFunctions_FHIR4.cql");
        matGlobalCommonFunctionsFhir4Library = createLib(matGlobalCommonFunctionsFhir4Cql);

        scratchCql = getStringFromResource("/includes/Scratch.cql");
        scratchLibrary = createLib(scratchCql);
    }

    @Test
    void processNoIncludes() {
        when(fhirIncludeLibraryProcessor.findIncludedFhirLibraries(scratchCql))
                .thenReturn(createScratchResponse());

        FhirIncludeLibraryResult result = libraryIncludeProcessor.process(scratchLibrary);

        assertEquals(scratchLibrary.getName(), result.getLibraryName());
        assertEquals(scratchLibrary.getVersion(), result.getLibraryVersion());

        assertTrue(CollectionUtils.isEmpty(result.getLibraryReferences()));

        verify(fhirIncludeLibraryProcessor).findIncludedFhirLibraries(scratchCql);
        verifyNoMoreInteractions(fhirIncludeLibraryProcessor);
    }

    @Test
    void processNoAttachments() {
        scratchLibrary.setContent(Collections.emptyList());

        Assertions.assertThrows(LibraryAttachmentNotFoundException.class, () -> {
            libraryIncludeProcessor.process(scratchLibrary);
        });

        verifyNoInteractions(fhirIncludeLibraryProcessor);
    }

    @Test
    void processWrongAttachmentType() {
        scratchLibrary.getContent().get(0).setContentType("mickey_mouse");

        Assertions.assertThrows(LibraryAttachmentNotFoundException.class, () -> {
            libraryIncludeProcessor.process(scratchLibrary);
        });

        verifyNoInteractions(fhirIncludeLibraryProcessor);
    }

    @Test
    void processContainsIncludes() {
        when(fhirIncludeLibraryProcessor.findIncludedFhirLibraries(qiCorePatternCql))
                .thenReturn(createQiCoreResponse());
        when(fhirIncludeLibraryProcessor.findIncludedFhirLibraries(fhirHelpersCql))
                .thenReturn(createFhirHelpersResponse());
        when(fhirIncludeLibraryProcessor.findIncludedFhirLibraries(matGlobalCommonFunctionsFhir4Cql))
                .thenReturn(createMatGlobalCommonFunctionsFhir4Response());
        when(fhirIncludeLibraryProcessor.findIncludedFhirLibraries(scratchCql))
                .thenReturn(createScratchResponse());

        FhirIncludeLibraryResult result = libraryIncludeProcessor.process(qiCorePatternLibrary);

        assertEquals(qiCorePatternLibrary.getName(), result.getLibraryName());
        assertEquals(qiCorePatternLibrary.getVersion(), result.getLibraryVersion());

        assertEquals(3, result.getLibraryReferences().size());

        assertTrue(result.getLibraryReferences().stream().
                allMatch(FhirIncludeLibraryReferences::isScannedForIncludedLibraries));

        assertTrue(containsLibrary(result.getLibraryReferences(), fhirHelpersLibrary));
        assertTrue(containsLibrary(result.getLibraryReferences(), matGlobalCommonFunctionsFhir4Library));
        assertTrue(containsLibrary(result.getLibraryReferences(), scratchLibrary));

        verify(fhirIncludeLibraryProcessor).findIncludedFhirLibraries(qiCorePatternCql);
        verify(fhirIncludeLibraryProcessor).findIncludedFhirLibraries(fhirHelpersCql);
        verify(fhirIncludeLibraryProcessor).findIncludedFhirLibraries(matGlobalCommonFunctionsFhir4Cql);
        verify(fhirIncludeLibraryProcessor).findIncludedFhirLibraries(scratchCql);

        verifyNoMoreInteractions(fhirIncludeLibraryProcessor);
    }

    private boolean containsLibrary(Set<FhirIncludeLibraryReferences> libraryReferences, Library library) {
        return libraryReferences.stream()
                .map(FhirIncludeLibraryReferences::getLibrary)
                .anyMatch(l -> l.equals(library));
    }

    private FhirIncludeLibraryResult createScratchResponse() {
        return buildResult(scratchLibrary);
    }

    private FhirIncludeLibraryResult createMatGlobalCommonFunctionsFhir4Response() {
        FhirIncludeLibraryResult result = buildResult(matGlobalCommonFunctionsFhir4Library);

        result.setLibraryReferences(new HashSet<>());
        result.getLibraryReferences().add(buildReference(fhirHelpersLibrary));
        result.getLibraryReferences().add(buildReference(scratchLibrary));

        return result;
    }

    private FhirIncludeLibraryResult createFhirHelpersResponse() {
        return buildResult(fhirHelpersLibrary);
    }

    private FhirIncludeLibraryResult createQiCoreResponse() {
        FhirIncludeLibraryResult result = buildResult(qiCorePatternLibrary);

        result.setLibraryReferences(new HashSet<>());
        result.getLibraryReferences().add(buildReference(fhirHelpersLibrary));
        result.getLibraryReferences().add(buildReference(matGlobalCommonFunctionsFhir4Library));

        return result;
    }

    private FhirIncludeLibraryReferences buildReference(Library library) {
        return FhirIncludeLibraryReferences.builder()
                .name(library.getName())
                .version(library.getVersion())
                .referenceEndpoint("http://library.cql.com")
                .searchResult(Boolean.TRUE)
                .library(library)
                .build();
    }

    private FhirIncludeLibraryResult buildResult(Library library) {
        return FhirIncludeLibraryResult.builder()
                .libraryName(library.getName())
                .libraryVersion(library.getVersion())
                .outcome(Boolean.TRUE)
                .build();
    }
}