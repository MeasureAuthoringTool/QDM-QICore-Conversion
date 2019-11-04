package gov.cms.mat.fhir.services.components.vsac;

import gov.cms.mat.fhir.services.config.VsacConfig;
import gov.cms.mat.vsac.VsacRestClient;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;
import org.vsac.VSACResponseResult;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@Slf4j
/**
 * Supporting vsac api documents
 * https://www.nlm.nih.gov/vsac/support/usingvsac/vsacsvsapiv2.html
 */
public class VsacClient {
    // this is what mat has by profile -- mat.qdm.default.expansion.id=Most Recent Code System Versions in VSAC
    private static final String PROFILE = "Most Recent Code System Versions in VSAC";

    private final boolean useCache; // By saving the data will speed things up for testing.
    private final String cacheDirectory;

    @Getter
    private final VsacRestClient vGroovyClient;

    public VsacClient(VsacConfig vsacConfig) {
        vGroovyClient = new VsacRestClient(vsacConfig.getProxyHost(),
                vsacConfig.getProxyPort(),
                vsacConfig.getServer(),
                vsacConfig.getService(),
                vsacConfig.getRetrieveMultiOidsService(),
                vsacConfig.getProfileService(),
                vsacConfig.getVersionService(),
                vsacConfig.getVsacServerDrcUrl());

        useCache = vsacConfig.isUseCache();
        cacheDirectory = vsacConfig.getCacheDirectory();
        if (useCache) {
            checkCacheDir();
        }
    }

    private void checkCacheDir() {
        Path cachePath = Paths.get(cacheDirectory);

        if (!cachePath.toFile().isDirectory()) {
            throw new IllegalArgumentException("Cannot find cache directory " + cacheDirectory);
        } else {
            if (!cachePath.toFile().canWrite()) {
                throw new IllegalArgumentException("Cannot write to  directory " + cacheDirectory);
            }
        }
    }

    public String getGrantingTicket(String userName, String password) {
        return vGroovyClient.getTicketGrantingTicket(userName, password);
    }

    public String getServiceTicket(String grantingTicket) {
        return vGroovyClient.getServiceTicket(grantingTicket);
    }

    public VSACResponseResult getDataFromProfile(String oid, String serviceTicket) {
        if (useCache) {
            Path cacheFilePath = createCacheFilePath(oid);

            if (cacheFilePath.toFile().exists()) {
                log.info("Xml is in cache for oid: {}", oid);
                return getFromFileCache(cacheFilePath);
            } else {
                log.trace("Xml is not in cache for path: {}", cacheFilePath);
            }
        }

        VSACResponseResult vsacResponseResult = vGroovyClient.getVsacDataForConversion(oid, serviceTicket, PROFILE);

        if (vsacResponseResult.isIsFailResponse()) {
            log.debug("vsacResponseResult failed with reason: {}", vsacResponseResult.getFailReason());
        } else {
            if (useCache) {
                Path cacheFilePath = createCacheFilePath(oid);
                writeFileToCache(cacheFilePath, vsacResponseResult.getXmlPayLoad());
            }
        }

        return vsacResponseResult;
    }

    private void writeFileToCache(Path cacheFilePath, String xmlPayLoad) {
        try {
            byte[] bytes = xmlPayLoad.getBytes();

            if (ArrayUtils.isEmpty(bytes)) {
                log.warn("Did not write file {}, bytes are empty!", cacheFilePath);
            } else {
                Files.write(cacheFilePath, bytes);
                log.info("Created {} to the file cache, byte count: {}", cacheFilePath, bytes.length);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Path createCacheFilePath(String oid) {
        return Paths.get(cacheDirectory, (oid + ".xml"));
    }

    private VSACResponseResult getFromFileCache(Path cacheFilePath) {
        try {
            String xml = new String(Files.readAllBytes(cacheFilePath));
            return createResponseResultFromCacheData(xml);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private VSACResponseResult createResponseResultFromCacheData(String xml) {
        VSACResponseResult vsacResponseResult = new VSACResponseResult();
        vsacResponseResult.setXmlPayLoad(xml);
        vsacResponseResult.setIsFailResponse(false);
        return vsacResponseResult;
    }
}
