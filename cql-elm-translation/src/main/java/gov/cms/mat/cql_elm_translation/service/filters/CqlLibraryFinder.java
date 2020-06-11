package gov.cms.mat.cql_elm_translation.service.filters;

import gov.cms.mat.cql.CqlTextParser;
import gov.cms.mat.cql.elements.LibraryProperties;

public interface CqlLibraryFinder {
    String getCqlData();

    default LibraryProperties parseLibrary() {
        CqlTextParser cqlTextParser = new CqlTextParser(getCqlData());
        return cqlTextParser.getLibrary();
    }
}
