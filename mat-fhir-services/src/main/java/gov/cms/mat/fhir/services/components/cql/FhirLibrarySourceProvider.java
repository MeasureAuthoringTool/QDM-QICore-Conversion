package gov.cms.mat.fhir.services.components.cql;

import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import org.apache.commons.io.IOUtils;
import org.cqframework.cql.cql2elm.LibrarySourceProvider;
import org.hl7.elm.r1.VersionedIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Base64;

@Component
public class FhirLibrarySourceProvider implements LibrarySourceProvider {

    @Autowired
    private HapiFhirServer hapiFhirServer;

    @Override
    public InputStream getLibrarySource(VersionedIdentifier versionedIdentifier) {

        var lib = hapiFhirServer.fetchHapiLibrary(versionedIdentifier.getId(), versionedIdentifier.getVersion());
        if (lib.isPresent()) {
            byte[] bytes = lib.get().getContent().get(0).getData();
            String cql = new String(Base64.getDecoder().decode(bytes));
            try {
                return IOUtils.toInputStream(cql, "utf-8");
            } catch (Exception e) {
                throw new RuntimeException("Could not find lib " + versionedIdentifier.getId()  + "V" + versionedIdentifier.getVersion(), e);
            }
        } else {
            throw new RuntimeException("Could not find lib " + versionedIdentifier.getId()  + "V" + versionedIdentifier.getVersion());
        }
    }

    private static String createKey(String name, String qdmVersion, String version) {
        return name + "-" + qdmVersion + "-" + version;
    }
}
