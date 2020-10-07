package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.services.exceptions.HapiResourceNotFoundException;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.translate.LibraryMapper;
import org.hl7.fhir.r4.model.Library;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LibraryControllerTest {
    private static final String ID = "id";
    private static final String JSON = "json";

    @Mock
    private HapiFhirServer hapiFhirServer;
    @Mock
    private LibraryMapper libraryMapper;

    @InjectMocks
    private LibraryController libraryController;

    @Test
    void translateLibraryByMeasureId() {
        Library library = new Library();
        when(hapiFhirServer.fetchHapiLibrary(ID)).thenReturn(Optional.of(library));
        when(hapiFhirServer.toJson(library)).thenReturn(JSON);

        String result = libraryController.findOne(ID);
        assertEquals(JSON, result);

        verify(hapiFhirServer).fetchHapiLibrary(ID);
        verify(hapiFhirServer).toJson(library);
    }

    @Test
    void translateLibraryByMeasureId_ThrowsHapiResourceNotFoundException() {
        when(hapiFhirServer.fetchHapiLibrary(ID)).thenReturn(Optional.empty());

        Assertions.assertThrows(HapiResourceNotFoundException.class, () -> {
            libraryController.findOne(ID);
        });

        verify(hapiFhirServer).fetchHapiLibrary(ID);
    }

    @Test
    void countValueSets() {
        when(libraryMapper.count()).thenReturn(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, libraryController.count());
    }

    @Test
    void deleteValueSets() {
        when(libraryMapper.deleteAll()).thenReturn(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, libraryController.deleteAll());
    }
}