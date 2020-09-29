package gov.cms.mat.fhir.services.components.cql;

import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import org.apache.commons.io.IOUtils;
import org.cqframework.cql.cql2elm.LibrarySourceProvider;
import org.hl7.elm.r1.VersionedIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class FhirLibrarySourceProvider implements LibrarySourceProvider {
    private final HapiFhirServer hapiFhirServer;

    public FhirLibrarySourceProvider(HapiFhirServer hapiFhirServer) {
        this.hapiFhirServer = hapiFhirServer;
    }

    @Override
    public InputStream getLibrarySource(VersionedIdentifier versionedIdentifier) {
        var lib = hapiFhirServer.fetchHapiLibrary(versionedIdentifier.getId(), versionedIdentifier.getVersion());
        if (lib.isPresent()) {
            byte[] bytes = lib.get().getContent().get(0).getData();
            try {
                return IOUtils.toInputStream(new String(bytes), "utf-8");
            } catch (Exception e) {
                throw new RuntimeException("Unknown Error with lib " + versionedIdentifier.getId() + "V" + versionedIdentifier.getVersion(), e);
            }
        } else {
            throw new RuntimeException("Could not find lib " + versionedIdentifier.getId() + "V" + versionedIdentifier.getVersion());
        }
    }
}
