package gov.cms.mat.fhir.services.components.fhir;

import gov.cms.mat.fhir.rest.dto.FhirIncludeLibraryReferences;
import gov.cms.mat.fhir.rest.dto.FhirIncludeLibraryResult;
import gov.cms.mat.fhir.services.BundleTestHelper;
import gov.cms.mat.fhir.services.ResourceFileUtil;
import gov.cms.mat.fhir.services.exceptions.CqlNotFhirException;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.service.packaging.LibraryHelper;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Library;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FhirIncludeLibraryProcessorTest implements ResourceFileUtil, BundleTestHelper, LibraryHelper {
    private static final String FULL_URL = "http://your.goofy.com/fullStop";

    @Mock
    private HapiFhirServer hapiFhirServer;

    @InjectMocks
    private FhirIncludeLibraryProcessor fhirIncludeLibraryProcessor;

    @Test
    void findIncludedFhirLibrariesLibraryReferenceFound() {
        String name = "MATGlobalCommonFunctions_FHIR";
        String version = "4.1.000";

        when(hapiFhirServer.fetchLibraryBundleByVersionAndName(version, name))
                .thenReturn(createBundle(FULL_URL, new Library()));

        String cql = getStringFromResource("/SepsisLactateClearanceRate_FHIR_1.0.001.cql");

        FhirIncludeLibraryResult fhirIncludeLibraryResult = fhirIncludeLibraryProcessor.findIncludedFhirLibraries(cql);
        assertEquals("SepsisLactateClearanceRate", fhirIncludeLibraryResult.getLibraryName());
        assertEquals("1.0.001", fhirIncludeLibraryResult.getLibraryVersion());

        assertEquals(1, fhirIncludeLibraryResult.getLibraryReferences().size());

        FhirIncludeLibraryReferences reference = fhirIncludeLibraryResult.getLibraryReferences().iterator().next();

        assertEquals(name, reference.getName());
        assertEquals(version, reference.getVersion());
        assertEquals(FULL_URL + "/Library/1", reference.getReferenceEndpoint());

        verify(hapiFhirServer)
                .fetchLibraryBundleByVersionAndName(version, name);
    }

    @Test
    void findIncludedFhirLibrariesManyLibraryReferencesFound() {
        when(hapiFhirServer.fetchLibraryBundleByVersionAndName(anyString(), anyString()))
                .thenReturn(createBundle(FULL_URL, new Library()));

        String cql = getStringFromResource("/fhir_include_many.cql");

        FhirIncludeLibraryResult fhirIncludeLibraryResult = fhirIncludeLibraryProcessor.findIncludedFhirLibraries(cql);
        verifyManyProcessed(fhirIncludeLibraryResult);

        assertEquals(3, fhirIncludeLibraryResult.getLibraryReferences().size());
    }

    private void verifyManyProcessed(FhirIncludeLibraryResult fhirIncludeLibraryResult) {
        assertEquals("TJCOverall_FHIR4", fhirIncludeLibraryResult.getLibraryName());
        assertEquals("4.0.000", fhirIncludeLibraryResult.getLibraryVersion());
        assertEquals(3, fhirIncludeLibraryResult.getLibraryReferences().size());

        verify(hapiFhirServer, times(3))
                .fetchLibraryBundleByVersionAndName(anyString(), anyString());
    }

    @Test
    void findIncludedFhirLibrariesManyLibraryReferencesNotFound() {
        when(hapiFhirServer.fetchLibraryBundleByVersionAndName(anyString(), anyString()))
                .thenReturn(new Bundle());

        String cql = getStringFromResource("/fhir_include_many.cql");

        FhirIncludeLibraryResult fhirIncludeLibraryResult = fhirIncludeLibraryProcessor.findIncludedFhirLibraries(cql);
        verifyManyProcessed(fhirIncludeLibraryResult);

        fhirIncludeLibraryResult.getLibraryReferences().forEach(i -> assertFalse(i.isSearchResult()));

        verify(hapiFhirServer, times(3))
                .fetchLibraryBundleByVersionAndName(anyString(), anyString());
    }

    @Test
    void findIncludedFhirLibrariesManyLibraryReferencesMoreThanOneFound() {
        when(hapiFhirServer.fetchLibraryBundleByVersionAndName(anyString(), anyString()))
                .thenReturn(createBundle(FULL_URL, new Library(), new Library(), new Library()));

        String cql = getStringFromResource("/fhir_include_many.cql");

        FhirIncludeLibraryResult fhirIncludeLibraryResult = fhirIncludeLibraryProcessor.findIncludedFhirLibraries(cql);
        verifyManyProcessed(fhirIncludeLibraryResult);

        fhirIncludeLibraryResult.getLibraryReferences().forEach(i -> assertFalse(i.isSearchResult()));

        verify(hapiFhirServer, times(3))
                .fetchLibraryBundleByVersionAndName(anyString(), anyString());
    }

    @Test
    void findIncludedFhirLibrariesNotFhir() {
        String cql = getStringFromResource("/called.cql");

        Assertions.assertThrows(CqlNotFhirException.class,
                () -> fhirIncludeLibraryProcessor.findIncludedFhirLibraries(cql));
    }

    @Test
    void findIncludedFhirLibrariesEmptyCql() {
        String cql = "";

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                fhirIncludeLibraryProcessor.findIncludedFhirLibraries(cql));
    }

    @Test
    void findIncludedFhirLibrariesIncludedLibraryReferencesFound() {
        String fhirHelpersCql = getStringFromResource("/includes/FHIRHelpers.cql");
        Bundle fhirHelpersBundle = createBundle(FULL_URL, createLib(fhirHelpersCql));
        lenient().when(hapiFhirServer.fetchLibraryBundleByVersionAndName("4.0.000", "FHIRHelpers"))
                .thenReturn(fhirHelpersBundle);

        String scratchCql = getStringFromResource("/includes/Scratch.cql");
        Bundle scratchBundle = createBundle(FULL_URL, createLib(scratchCql));
        lenient().when(hapiFhirServer.fetchLibraryBundleByVersionAndName("4.0.000", "Scratch"))
                .thenReturn(scratchBundle);

        String cql = getStringFromResource("/includes/MATGlobalCommonFunctions_FHIR4.cql");
        FhirIncludeLibraryResult fhirIncludeLibraryResult = fhirIncludeLibraryProcessor.findIncludedFhirLibraries(cql);

        assertEquals(2, fhirIncludeLibraryResult.getLibraryReferences().size());
    }
}