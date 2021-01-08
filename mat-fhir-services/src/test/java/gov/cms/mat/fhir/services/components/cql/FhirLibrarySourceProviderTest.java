package gov.cms.mat.fhir.services.components.cql;

import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import org.hl7.elm.r1.VersionedIdentifier;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Library;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FhirLibrarySourceProviderTest {
    private static final String VERSION = "version";
    private static final String ID = "id";

    @Mock
    private HapiFhirServer hapiFhirServer;

    @InjectMocks
    private FhirLibrarySourceProvider fhirLibrarySourceProvider;

    private VersionedIdentifier versionedIdentifier;

    @BeforeEach
    void setUp() {
        versionedIdentifier = new VersionedIdentifier();
        versionedIdentifier.setId(ID);
        versionedIdentifier.setVersion(VERSION);
    }

    @Test
    void getLibrarySourceNotFound() {
        when(hapiFhirServer.fetchHapiLibrary(ID, VERSION)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            fhirLibrarySourceProvider.getLibrarySource(versionedIdentifier);
        });

        assertTrue(exception.getMessage().startsWith("Could not find lib "));
    }

    @Test
    void getLibrarySourceError() {
        Library library = new Library();
        library.setContent(List.of(new Attachment())); // payload bytes are null

        when(hapiFhirServer.fetchHapiLibrary(ID, VERSION)).thenReturn(Optional.of(library));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            fhirLibrarySourceProvider.getLibrarySource(versionedIdentifier);
        });

        assertTrue(exception.getMessage().startsWith("Unknown Error with lib "));
    }

    @Test
    void getLibrarySource() throws IOException {
        Attachment attachment = new Attachment();
        attachment.setData(  "data".getBytes());
        Library library = new Library();
        library.setContent(List.of(attachment));

        when(hapiFhirServer.fetchHapiLibrary(ID, VERSION)).thenReturn(Optional.of(library));

        InputStream inputStream = fhirLibrarySourceProvider.getLibrarySource(versionedIdentifier);

        assertEquals("data", new String(inputStream.readAllBytes()));
    }
}