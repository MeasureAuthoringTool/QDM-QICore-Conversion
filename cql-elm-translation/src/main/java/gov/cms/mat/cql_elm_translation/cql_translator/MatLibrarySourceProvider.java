package gov.cms.mat.cql_elm_translation.cql_translator;

import org.cqframework.cql.cql2elm.FhirLibrarySourceProvider;
import org.cqframework.cql.cql2elm.LibrarySourceProvider;
import org.hl7.elm.r1.VersionedIdentifier;

import java.io.InputStream;

public class MatLibrarySourceProvider implements LibrarySourceProvider {

    @Override
    public InputStream getLibrarySource(VersionedIdentifier libraryIdentifier) {

        if (libraryIdentifier.getId().toLowerCase().contains("fhir")) {
            return FhirLibrarySourceProvider.class.getResourceAsStream(String.format("/org/hl7/fhir/%s-%s.cql", libraryIdentifier.getId(),
                    libraryIdentifier.getVersion()));
        } else {
            return FhirLibrarySourceProvider.class.getResourceAsStream(String.format("/%s-%s.cql", libraryIdentifier.getId(),
                    libraryIdentifier.getVersion()));
        }
    }
}
