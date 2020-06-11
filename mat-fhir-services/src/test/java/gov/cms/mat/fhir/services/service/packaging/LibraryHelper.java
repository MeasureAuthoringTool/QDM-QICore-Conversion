package gov.cms.mat.fhir.services.service.packaging;

import gov.cms.mat.cql.CqlTextParser;
import gov.cms.mat.cql.elements.LibraryProperties;
import org.hl7.fhir.r4.model.Library;

import java.util.Base64;
import java.util.UUID;

import static gov.cms.mat.fhir.services.translate.LibraryTranslatorBase.CQL_CONTENT_TYPE;

public interface LibraryHelper {
    default Library createLib(String cql) {
        CqlTextParser cqlTextParser = new CqlTextParser(cql);

        LibraryProperties libraryProperties = cqlTextParser.getLibrary();

        Library library = new Library();
        library.setId(UUID.randomUUID().toString());
        library.setUrl("http://hapi.joyjoy.com/" + library.getId());

        library.setName(libraryProperties.getName());
        library.setVersion(libraryProperties.getVersion());
        library.addContent().setContentType(CQL_CONTENT_TYPE).setData(Base64.getEncoder().encode(cql.getBytes()));
        return library;
    }

}
