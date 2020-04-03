package gov.cms.mat.cql_elm_translation.service.filters;

import gov.cms.mat.cql.CqlParser;
import gov.cms.mat.cql.elements.LibraryProperties;

public interface CqlLibraryFinder {
    String getCqlData();

    default LibraryProperties parseLibrary() {
        CqlParser cqlParser = new CqlParser(getCqlData());
        return cqlParser.getLibrary();
    }
}
