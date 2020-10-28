package gov.cms.mat.fhir.services.service.packaging;

import gov.cms.mat.cql.CqlTextParser;
import gov.cms.mat.cql.elements.LibraryProperties;
import org.hl7.fhir.r4.model.Library;

import java.util.Base64;
import java.util.UUID;

public interface LibraryHelper {
    default Library createLib(String cql) {
        CqlTextParser cqlTextParser = new CqlTextParser(cql);

        LibraryProperties libraryProperties = cqlTextParser.getLibrary();

        Library library = new Library();
        library.setId(UUID.randomUUID().toString());
        library.setUrl("http://hapi.joyjoy.com/" + library.getId());

        library.setName(libraryProperties.getName());
        library.setVersion(libraryProperties.getVersion());
        library.addContent().setContentType("text/cql").setData(cql.getBytes());
        return library;
    }

}
