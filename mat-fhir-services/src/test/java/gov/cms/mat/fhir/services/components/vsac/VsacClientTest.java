package gov.cms.mat.fhir.services.components.vsac;

import gov.cms.mat.fhir.services.config.VsacConfig;
import gov.cms.mat.vsac.VsacRestClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vsac.VSACResponseResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class VsacClientTest {
    private static final String OID = "oid";
    private static final String SERVICE_TICKET = "ticket";
    private static final String XML = "<xml>XML</xml>";

    @TempDir
    File tempDir;

    @Mock
    private VsacConfig vsacConfig;
    @Mock
    private VsacRestClient vGroovyClient;
    @InjectMocks
    private VsacClient vsacClient;

    @Test
    void checkCacheDir_NoCache() {
        when(vsacConfig.isUseCache()).thenReturn(false);
        vsacClient.checkCacheDir();

        verify(vsacConfig, never()).getCacheDirectory();  // wont call the getter NO need to verify cache dir
    }

    @Test
    void checkCacheDir_UseCache() {
        when(vsacConfig.getCacheDirectory()).thenReturn(tempDir.getAbsolutePath());
        when(vsacConfig.isUseCache()).thenReturn(true);
        vsacClient.checkCacheDir();

        //now calls the getter needs to verify cache dir and the log info message
        verify(vsacConfig, times(2)).getCacheDirectory();
    }

    @Test
    void checkCacheDir_BadCacheDir() {
        String badDirectoryName = "\"%@~%^^'?$__--\"";
        when(vsacConfig.getCacheDirectory()).thenReturn(badDirectoryName);
        when(vsacConfig.isUseCache()).thenReturn(true);

        IllegalArgumentException thrown =
                Assertions.assertThrows(IllegalArgumentException.class, () -> {
                    vsacClient.checkCacheDir();
                });

        // once to get dir to verify, and again for error message
        verify(vsacConfig, times(2)).getCacheDirectory();

        assertTrue(thrown.getMessage().contains("Cannot find cache directory"));
        assertTrue(thrown.getMessage().contains(badDirectoryName));
    }

    @Test
    void checkCacheDir_CannotWrite() {
        when(vsacConfig.getCacheDirectory()).thenReturn(tempDir.getAbsolutePath());
        when(vsacConfig.isUseCache()).thenReturn(true);

        assertTrue(tempDir.setWritable(false));

        IllegalArgumentException thrown =
                Assertions.assertThrows(IllegalArgumentException.class, () -> {
                    vsacClient.checkCacheDir();
                });

        // once to get dir to verify, and again for error message
        verify(vsacConfig, times(2)).getCacheDirectory();
        assertTrue(thrown.getMessage().contains("Cannot write to directory"));
        assertTrue(thrown.getMessage().contains(tempDir.getAbsolutePath()));

        assertTrue(tempDir.setWritable(true));
    }

    @Test
    void getDataFromProfile_NoCacheBadResponse() {
        when(vsacConfig.isUseCache()).thenReturn(false);
        when(vsacConfig.getVsacRestClient()).thenReturn(vGroovyClient);

        VSACResponseResult toReturn = createResponseResult(true, null);

        VSACResponseResult returnedFromClient = vsacClient.getDataFromProfile(OID, SERVICE_TICKET);

        assertEquals(toReturn, returnedFromClient);

        verify(vGroovyClient).getVsacDataForConversion(OID, SERVICE_TICKET, VsacClient.PROFILE);
    }

    @Test
    void getDataFromProfile_AddToCache() {
        when(vsacConfig.isUseCache()).thenReturn(true);
        when(vsacConfig.getCacheDirectory()).thenReturn(tempDir.getAbsolutePath());
        when(vsacConfig.getVsacRestClient()).thenReturn(vGroovyClient);

        VSACResponseResult toReturn = createResponseResult(false, XML);

        VSACResponseResult returnedFromClient = vsacClient.getDataFromProfile(OID, SERVICE_TICKET);

        assertEquals(toReturn, returnedFromClient);

        verify(vGroovyClient).getVsacDataForConversion(OID, SERVICE_TICKET, VsacClient.PROFILE);

        assertTrue(this::oidXMlExists);
    }

    @Test
    void getDataFromProfile_DontAddToCacheXmlEmpty() {
        when(vsacConfig.isUseCache()).thenReturn(true);
        when(vsacConfig.getCacheDirectory()).thenReturn(tempDir.getAbsolutePath());
        when(vsacConfig.getVsacRestClient()).thenReturn(vGroovyClient);

        VSACResponseResult toReturn = createResponseResult(false, ""); // this does happen

        VSACResponseResult returnedFromClient = vsacClient.getDataFromProfile(OID, SERVICE_TICKET);

        assertEquals(toReturn, returnedFromClient);

        assertFalse(this::oidXMlExists); // Wont write if xml is empty
    }

    @Test
    void getDataFromProfile_GetFromCache() throws IOException {
        Files.write(makeOidXmlFileCachePath(), XML.getBytes());
        assertTrue(this::oidXMlExists);

        when(vsacConfig.isUseCache()).thenReturn(true);
        when(vsacConfig.getCacheDirectory()).thenReturn(tempDir.getAbsolutePath());

        VSACResponseResult returnedFromClient = vsacClient.getDataFromProfile(OID, SERVICE_TICKET);

        assertFalse(returnedFromClient.isIsFailResponse());
        assertEquals(XML, returnedFromClient.getXmlPayLoad());

        assertTrue(this::oidXMlExists); // verify still exists
    }

    private boolean oidXMlExists() {
        return Files.exists(makeOidXmlFileCachePath());
    }

    private Path makeOidXmlFileCachePath() {
        return Paths.get(tempDir.getPath(), OID + ".xml");
    }

    private VSACResponseResult createResponseResult(boolean isFail, String xml) {
        VSACResponseResult vsacResponseResult = new VSACResponseResult();
        vsacResponseResult.setIsFailResponse(isFail);
        vsacResponseResult.setXmlPayLoad(xml);

        when(vGroovyClient.getVsacDataForConversion(OID, SERVICE_TICKET, VsacClient.PROFILE)).thenReturn(vsacResponseResult);

        return vsacResponseResult;
    }
}