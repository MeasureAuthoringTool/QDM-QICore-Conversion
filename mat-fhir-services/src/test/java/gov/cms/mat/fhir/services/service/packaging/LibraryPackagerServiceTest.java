package gov.cms.mat.fhir.services.service.packaging;

import gov.cms.mat.fhir.services.components.fhir.FhirIncludeLibraryProcessor;
import gov.cms.mat.fhir.services.exceptions.HapiResourceNotFoundException;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Library;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LibraryPackagerServiceTest implements LibraryHelper {
    private final String ID = "id";

    @Mock
    private HapiFhirServer hapiFhirServer;
    @Mock
    private FhirIncludeLibraryProcessor fhirIncludeLibraryProcessor;

    @InjectMocks
    private LibraryPackagerService libraryPackagerService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void packageMinimumNotFoundInHapi() {
        when(hapiFhirServer.getLibraryBundle(ID)).thenReturn(new Bundle());

        Assertions.assertThrows(HapiResourceNotFoundException.class, () -> {
            libraryPackagerService.packageMinimum(ID);
        });
    }

    @Test
    void packageMinimumFoundInHapi() {

        //todo get lib from cql
//        Bundle bundle = new Bundle();
//
//        Bundle.BundleEntryComponent first = bundle.addEntry();
//        first.setResource(new Library());
//
//        Bundle.BundleEntryComponent second = bundle.addEntry();
//        second.setResource(new Library());
//
//        assertTrue(bundle.hasEntry());
//
//        when(hapiFhirServer.getLibraryBundle(ID)).thenReturn(bundle);
//
//        Assertions.assertThrows(HapiResourceNotFoundException.class, () -> {
//            libraryPackagerService.packageMinimum(ID);
//        });
    }




    @Test
    void fetchLibraryFromHapi() {
    }

    @Test
    void packageFull() {
    }

    @Test
    void buildIncludeBundle() {
    }
}