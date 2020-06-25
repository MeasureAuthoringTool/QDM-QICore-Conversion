package gov.cms.mat.fhir.services.components.validation;

import gov.cms.mat.cql.CqlTextParser;
import gov.cms.mat.cql.elements.LibraryProperties;
import gov.cms.mat.fhir.services.rest.dto.LibraryErrors;
import mat.shared.CQLError;

abstract class CqlValidatorHelper {
    LibraryErrors buildLibraryErrors(CqlTextParser cqlTextParser) {
        LibraryProperties libraryProperties = cqlTextParser.getLibrary();
        return new LibraryErrors(libraryProperties.getName(), libraryProperties.getVersion());
    }

    protected CQLError createCqlError(String message, int lineIndex, int lineLength) {
        CQLError cqlError = new CQLError();
        cqlError.setSeverity("Error");
        cqlError.setErrorMessage(message);
        cqlError.setStartErrorInLine(lineIndex);
        cqlError.setErrorInLine(lineIndex);

        cqlError.setErrorAtOffset(0);
        cqlError.setStartErrorAtOffset(0);

        cqlError.setEndErrorInLine(lineLength);
        cqlError.setErrorAtOffset(lineLength);

        return cqlError;
    }
}
