package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.service.packaging.LibraryPackagerService;
import gov.cms.mat.fhir.services.service.packaging.dto.LibraryPackageFullData;
import gov.cms.mat.fhir.services.service.packaging.dto.LibraryPackageFullHapi;
import gov.cms.mat.fhir.services.service.packaging.dto.PackageFormat;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Library;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LibraryPackagerControllerTest {
    private static final String ID = "id";

    @Mock
    private LibraryPackagerService libraryPackagerService;
    @Mock
    private HapiFhirServer hapiFhirServer;

    @InjectMocks
    LibraryPackagerController libraryPackagerController;

    @Test
    void packageMinimumJson() {
        Library library = new Library();

        when(libraryPackagerService.packageMinimum(ID)).thenReturn(library);
        when(hapiFhirServer.formatResource(library, PackageFormat.JSON)).thenReturn("LIBRARY_JSON");

        String libJson = libraryPackagerController.packageMinimumJson(ID);

        assertEquals("LIBRARY_JSON", libJson);
    }

    @Test
    void packageFullJson() {
        Library library = new Library();
        Bundle includeBundle = new Bundle();

        LibraryPackageFullHapi libraryPackageFullHapi = LibraryPackageFullHapi.builder()
                .library(library)
                .includeBundle(includeBundle)
                .build();

        when(libraryPackagerService.packageFull(ID)).thenReturn(libraryPackageFullHapi);
        when(hapiFhirServer.formatResource(library, PackageFormat.JSON)).thenReturn("LIBRARY");
        when(hapiFhirServer.formatResource(includeBundle, PackageFormat.JSON)).thenReturn("INCLUDE_BUNDLE");

        LibraryPackageFullData result = libraryPackagerController.packageFullJson(ID);

        assertEquals("LIBRARY", result.getLibrary());
        assertEquals("INCLUDE_BUNDLE", result.getIncludeBundle());
    }
}