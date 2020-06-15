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

    CQLError findLine(String oid, String[] lines) {
        int lineCounter = 1;
        int lineIndex = -1;
        int lineLength = -1;

        //filter by comment & todo antlr

        for (String cqlLine : lines) {
            if (cqlLine.contains(oid)) {
                lineLength = cqlLine.length();
                lineIndex = lineCounter;
                break;
            } else {
                lineCounter++;
            }
        }

        return createCqlError(oid, lineIndex, lineLength);
    }

    private CQLError createCqlError(String oid, int lineIndex, int lineLength) {
        CQLError cqlError = new CQLError();
        cqlError.setSeverity("Error");
        cqlError.setErrorMessage("Cannot find " +  getType() + " with oid: " + oid);
        cqlError.setStartErrorInLine(lineIndex);
        cqlError.setErrorInLine(lineIndex);

        cqlError.setErrorAtOffset(0);
        cqlError.setStartErrorAtOffset(0);

        cqlError.setEndErrorInLine(lineLength);
        cqlError.setErrorAtOffset(lineLength);

        return cqlError;
    }

    abstract String getType();
}
