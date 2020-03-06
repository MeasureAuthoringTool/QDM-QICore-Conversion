package gov.cms.mat.fhir.services.components.vsac;

import com.google.common.annotations.VisibleForTesting;
import gov.cms.mat.fhir.services.config.VsacConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;
import org.vsac.VSACResponseResult;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@Slf4j
/*
 * Supporting vsac api documents
 * https://www.nlm.nih.gov/vsac/support/usingvsac/vsacsvsapiv2.html
 */
public class VsacClient {
    // this is what MAT has by profile -- mat.qdm.default.expansion.id=Most Recent Code System Versions in VSAC
    static final String PROFILE = "Most Recent Code System Versions in VSAC";

    private final VsacConfig vsacConfig;


    public VsacClient(VsacConfig vsacConfig) {
        this.vsacConfig = vsacConfig;
    }

    @PostConstruct
    void checkCacheDir() {
        if (vsacConfig.isUseCache()) {
            verifyPath();
            log.info("VsacClient has file caching turned on. cacheDirectory: {}", vsacConfig.getCacheDirectory());
        } else {
            log.info("VsacClient has file caching turned off.");
        }
    }

    private void verifyPath() {
        Path cachePath = Paths.get(vsacConfig.getCacheDirectory());

        if (!cachePath.toFile().isDirectory()) {
            throw new IllegalArgumentException("Cannot find cache directory " + vsacConfig.getCacheDirectory());
        } else {
            if (!cachePath.toFile().canWrite()) {
                throw new IllegalArgumentException("Cannot write to directory " + vsacConfig.getCacheDirectory());
            }
        }
    }

    // For testing purposes only
    @VisibleForTesting
    public String getGrantingTicket(String userName, String password) {
        return vsacConfig.getVsacRestClient().getTicketGrantingTicket(userName, password);
    }

    public String getServiceTicket(String grantingTicket) {
        if (vsacConfig.isUseCacheOnly()) {
            return "isUseCacheOnly==true";
        }

        return vsacConfig.getVsacRestClient().getServiceTicket(grantingTicket);
    }

    public VSACResponseResult getDataFromProfile(String oid, String serviceTicket) {
        Path cacheFilePath = null;

        if (vsacConfig.isUseCache()) {
            cacheFilePath = createCacheFilePath(oid);

            if (cacheFilePath.toFile().exists()) {
                log.trace("Xml is in cache for oid: {}", oid);
                return getFromFileCache(cacheFilePath);
            } else {
                log.trace("Xml is not in cache for path: {}", cacheFilePath);

                if (vsacConfig.isUseCacheOnly()) {
                    return createFailureResponse();
                }
            }
        }

        VSACResponseResult vsacResponseResult =
                vsacConfig.getVsacRestClient().getVsacDataForConversion(oid, serviceTicket, PROFILE);

        if (vsacResponseResult.isIsFailResponse()) {
            log.debug("vsacResponseResult failed with reason: {}", vsacResponseResult.getFailReason());
        } else {
            if (vsacConfig.isUseCache()) {
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
                log.info("Created XML file {} and added to the file cache, byte count: {}", cacheFilePath, bytes.length);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Path createCacheFilePath(String oid) {
        return Paths.get(vsacConfig.getCacheDirectory(), (oid + ".xml"));
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

    private VSACResponseResult createFailureResponse() {
        VSACResponseResult vsacResponseResult = new VSACResponseResult();
        vsacResponseResult.setIsFailResponse(false);
        return vsacResponseResult;
    }
}
