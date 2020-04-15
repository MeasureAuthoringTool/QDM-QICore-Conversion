package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.rest.dto.cql.CqlPayload;
import gov.cms.mat.fhir.rest.dto.cql.CqlPayloadType;
import gov.cms.mat.fhir.services.exceptions.CqlLibraryNotFoundException;
import gov.cms.mat.fhir.services.exceptions.InvalidVersionException;
import gov.cms.mat.fhir.services.service.CqlLibraryDataService;
import gov.cms.mat.fhir.services.service.LibraryFinderService;
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
    @Mock
    private LibraryFinderService libraryFinderService;

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

        when(libraryFinderService.findLibrary(any())).thenThrow(new CqlLibraryNotFoundException("oops"));

        Assertions.assertThrows(CqlLibraryNotFoundException.class, () -> {
            libraryFinderController.findLibraryXml("qdmVersion", "name", "4.0.000", "QDM");
        });

        verify(libraryFinderService).findLibrary(any());
    }

    @Test
    void findLibraryXml_HappyCase() {
        String xml = "</xml>";

        CqlPayload cqlPayload = CqlPayload.builder()
                .type(CqlPayloadType.XML)
                .data(xml)
                .build();

        when(libraryFinderService.findLibrary(any())).thenReturn(cqlPayload);

        CqlPayload payload = libraryFinderController.findLibraryXml("qdmVersion", "name", "3.0.000", "QDM");

        assertEquals(xml, payload.getData());

        verify(libraryFinderService).findLibrary(any());
    }
}