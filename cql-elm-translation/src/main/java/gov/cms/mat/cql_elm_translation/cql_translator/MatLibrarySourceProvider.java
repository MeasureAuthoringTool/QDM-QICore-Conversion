package gov.cms.mat.cql_elm_translation.cql_translator;

import gov.cms.mat.cql.elements.UsingProperties;
import gov.cms.mat.cql_elm_translation.service.MatFhirServices;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.cqframework.cql.cql2elm.LibrarySourceProvider;
import org.hl7.elm.r1.VersionedIdentifier;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class MatLibrarySourceProvider implements LibrarySourceProvider {
    private static final ConcurrentHashMap<String, String> cqlLibraries = new ConcurrentHashMap<>();
    private static final ThreadLocal<UsingProperties> threadLocalValue = new ThreadLocal<>();
    private static MatFhirServices matFhirServices;

    public static void setFhirServicesService(MatFhirServices matFhirServices) {
        MatLibrarySourceProvider.matFhirServices = matFhirServices;
    }

    public static void setUsing(UsingProperties usingProperties) {
        threadLocalValue.set(usingProperties);
    }

    private static String createKey(String name, String qdmVersion, String version) {
        return name + "-" + qdmVersion + "-" + version;
    }

    @Override
    public InputStream getLibrarySource(VersionedIdentifier libraryIdentifier) {
        String usingVersion = threadLocalValue.get().getVersion(); //using FHIR version '4.0.0
        String key = createKey(libraryIdentifier.getId(), usingVersion, libraryIdentifier.getVersion());

        if (cqlLibraries.containsKey(key)) {
            return getInputStream(cqlLibraries.get(key)); // do we need to expire cache ?????
        } else {
            return processLibrary(libraryIdentifier, key);
        }
    }

    public InputStream processLibrary(VersionedIdentifier libraryIdentifier, String key) {
        if (threadLocalValue.get().getLibraryType().equals("QDM")) {
            throw new RuntimeException("QDM is not supported FHIR only.");
        } else if (threadLocalValue.get().getLibraryType().equals("FHIR")) {
            return getInputStream(libraryIdentifier, key);
        } else {
            log.error("Cannot process Library for key: {}", key);
            return null;
        }
    }

    public InputStream getInputStream(VersionedIdentifier libraryIdentifier, String key) {
        String cql = matFhirServices.getHapiFhirCql(libraryIdentifier.getId(),
                libraryIdentifier.getVersion());
        return processCqlFromService(key, cql);
    }

    private InputStream processCqlFromService(String key, String cql) {
        if (StringUtils.isEmpty(cql)) {
            log.debug("Did not find any cql for key : {}", key);
            return null;
        } else {
            cqlLibraries.put(key, cql);
            return getInputStream(cql);
        }
    }

    public InputStream getInputStream(String cql) {
        return IOUtils.toInputStream(cql, StandardCharsets.UTF_8);
    }
}
