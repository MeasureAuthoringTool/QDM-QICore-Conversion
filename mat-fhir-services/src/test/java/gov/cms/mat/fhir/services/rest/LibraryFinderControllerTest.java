package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.services.exceptions.CqlLibraryNotFoundException;
import gov.cms.mat.fhir.services.exceptions.InvalidVersionException;
import gov.cms.mat.fhir.services.service.CqlLibraryDataService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibraryFinderControllerTest {
    @Mock
    private CqlLibraryDataService cqlLibraryDataService;
    @InjectMocks
    private LibraryFinderController libraryFinderController;

    @Test
    void findLibraryXmlInvalidVersion() {
        Assertions.assertThrows(InvalidVersionException.class, () -> {
            libraryFinderController.findLibraryXml("qdmVersion", "name", "invalid_version", "QDM");
        });

        verifyNoInteractions(cqlLibraryDataService);
    }

    @Test
    void findLibraryXml_XmlNotFound() {
        CqlLibrary cqlLibrary = new CqlLibrary();
        cqlLibrary.setCqlXml(null);

        when(cqlLibraryDataService.findCqlLibrary(any())).thenReturn(cqlLibrary);

        Assertions.assertThrows(CqlLibraryNotFoundException.class, () -> {
            libraryFinderController.findLibraryXml("qdmVersion", "name", "4.0.000", "QDM");
        });

        verify(cqlLibraryDataService).findCqlLibrary(any());
    }

    @Test
    void findLibraryXml_HappyCase() {
        String xml = "</xml>";
        CqlLibrary cqlLibrary = new CqlLibrary();
        cqlLibrary.setCqlXml(xml);

        when(cqlLibraryDataService.findCqlLibrary(any())).thenReturn(cqlLibrary);

        String libraryXml = libraryFinderController.findLibraryXml("qdmVersion", "name", "3.0.000", "QDM");

        assertEquals(xml, libraryXml);

        verify(cqlLibraryDataService).findCqlLibrary(any());
    }
}