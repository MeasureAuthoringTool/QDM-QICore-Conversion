package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.rest.dto.FhirIncludeLibraryResult;
import gov.cms.mat.fhir.services.components.fhir.FhirIncludeLibraryProcessor;
import gov.cms.mat.fhir.services.service.CqlLibraryDataService;
import gov.cms.mat.fhir.services.service.LibraryFinderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LibraryFinderControllerTest {
    @Mock
    private FhirIncludeLibraryProcessor fhirIncludeLibraryProcessor;
    @Mock
    private LibraryFinderService libraryFinderService;

    @InjectMocks
    private LibraryFinderController libraryFinderController;

    @Test
    void findLibraryHapiCql() {
        when(libraryFinderService.getCqlFromFire("name", "version")).thenReturn("CqlFromFire");

        assertEquals("CqlFromFire",
                libraryFinderController.findLibraryHapiCql("name", "version"));

        verifyNoMoreInteractions(libraryFinderService);
        verifyNoInteractions(fhirIncludeLibraryProcessor);
    }
    @Test
    void findIncludedFhirLibraries() {
        FhirIncludeLibraryResult result =  FhirIncludeLibraryResult.builder().build();

        when( fhirIncludeLibraryProcessor.findIncludedFhirLibraries("cqlContent")).thenReturn(result);

        assertEquals(result, libraryFinderController.findIncludedFhirLibraries("cqlContent"));

        verifyNoMoreInteractions(fhirIncludeLibraryProcessor );
        verifyNoInteractions(libraryFinderService);
    }
}