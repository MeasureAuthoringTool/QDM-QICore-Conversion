package gov.cms.mat.cql_elm_translation.cql_translator;

import gov.cms.mat.cql.CqlParser;
import gov.cms.mat.cql_elm_translation.service.MatFhirServices;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.cqframework.cql.cql2elm.LibrarySourceProvider;
import org.hl7.elm.r1.VersionedIdentifier;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class MatLibrarySourceProvider implements LibrarySourceProvider {

    private static final ConcurrentHashMap<String, String> cqlLibraries = new ConcurrentHashMap<>();
    private static final ThreadLocal<CqlParser.UsingProperties> threadLocalValue = new ThreadLocal<>();
    private static MatFhirServices matFhirServices;

    public static void setFhirServicesService(MatFhirServices matFhirServices) {
        MatLibrarySourceProvider.matFhirServices = matFhirServices;
    }

    public static void setQdmVersion(CqlParser.UsingProperties usingProperties) {
        threadLocalValue.set(usingProperties);
    }

    public static boolean isLibraryInMap(String name, String qdmVersion, String version) {
        return cqlLibraries.contains(createKey(name, qdmVersion, version));
    }

    public static void addLibraryInMap(String name, String qdmVersion, String version, String cql) {
        cqlLibraries.put(createKey(name, qdmVersion, version), cql);
    }

    private static String createKey(String name, String qdmVersion, String version) {
        return name + "-" + qdmVersion + "-" + version;
    }

    @Override
    public InputStream getLibrarySource(VersionedIdentifier libraryIdentifier) {
        String usingVersion = threadLocalValue.get().getVersion(); //using FHIR version '4.0.0
        String key = createKey(libraryIdentifier.getId(), usingVersion, libraryIdentifier.getVersion());

        if (cqlLibraries.containsKey(key)) {
            return getInputStream(cqlLibraries.get(key));
        }

        if (threadLocalValue.get().getLibraryType().equals("FHIR")) {

            String cql = matFhirServices.getFhirCql(libraryIdentifier.getId(), libraryIdentifier.getVersion());

            //  String  cql = matFhirServices.getMatCql(libraryIdentifier.getId(), libraryIdentifier.getVersion(), usingVersion);

            if (StringUtils.isEmpty(cql)) {
                log.debug("Did not find any cql");
                return null;
            } else {
                cqlLibraries.put(key, cql);
                return getInputStream(cql);
            }
        } else {
            return null;
        }


//        if (libraryIdentifier.getId().toLowerCase().contains("fhir")) {
//            String resource = String.format("/org/hl7/fhir/%s-%s.cql",
//                    libraryIdentifier.getId(),
//                    libraryIdentifier.getVersion());
//
//            log.info("Loading FHIR library source: {}", resource);
//
//            return FhirLibrarySourceProvider.class.getResourceAsStream(resource);
//        } else {
//
//
//
//            String key = createKey(libraryIdentifier.getId(), usingVersion, libraryIdentifier.getVersion());
//
//            String cql = cqlLibraries.get(key);
//
//            if (StringUtils.isEmpty(cql)) {
//
//                cql = fhirServicesService.getCql(libraryIdentifier.getId(), libraryIdentifier.getVersion(), usingVersion);
//
//                if (StringUtils.isEmpty(cql)) {
//                    log.debug("Did not find any cql");
//                    return null;
//                } else {
//                    cqlLibraries.put(key, cql);
//                    return getInputStream(cql);
//                }
//            } else {
//                return getInputStream(cql);
//            }
//        }
    }

    public InputStream getInputStream(String cql) {
        try {
            return IOUtils.toInputStream(cql, "utf-8");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
