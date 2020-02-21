package gov.cms.mat.fhir.services.service.support;

import gov.cms.mat.fhir.rest.dto.CqlConversionError;
import gov.cms.mat.fhir.rest.dto.MatCqlConversionException;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.service.CQLLibraryTranslationService;

import java.util.List;

public interface LibraryConversionReporter {

    default void processCqlConversionResultSuccess(String matLibraryId) {
        if (matLibraryId != null) {
            ConversionReporter.setCqlConversionResultSuccess(matLibraryId);
        }
    }

    default void processCqlConversionErrors(String matLibraryId,
                                            CQLLibraryTranslationService.ConversionType conversionType,
                                            List<CqlConversionError> cqlConversionErrors) {
        if (matLibraryId != null) {
            switch (conversionType) {
                case QDM: {
                    ConversionReporter.setCqlConversionErrors(cqlConversionErrors, matLibraryId);
                    ConversionReporter.setCqlConversionErrorMessage("CQl-QDM conversion produced " + cqlConversionErrors.size()
                            + " cqlConversionErrors errors.", matLibraryId);
                    break;
                }
                case FHIR: {
                    ConversionReporter.setFhirCqlConversionErrors(cqlConversionErrors, matLibraryId);
                    ConversionReporter.setCqlConversionErrorMessage("CQl-Fhir conversion produced " + cqlConversionErrors.size()
                            + " cqlConversionErrors errors.", matLibraryId);
                    break;
                }
                default:
                    throw new IllegalArgumentException("Did not handle conversionType: " + conversionType);
            }
        }
    }

    default void processMatCqlConversionErrors(String matLibraryId,
                                               CQLLibraryTranslationService.ConversionType conversionType,
                                               List<MatCqlConversionException> matCqlConversionExceptions) {
        if (matLibraryId != null) {
            switch (conversionType) {
                case QDM: {
                    ConversionReporter.setCqlConversionErrorMessage("CQl-QDM conversion produced " + matCqlConversionExceptions.size()
                            + " matCqlConversionExceptions (errorExceptions) errors.", matLibraryId);
                    ConversionReporter.setMatCqlConversionExceptions(matCqlConversionExceptions, matLibraryId);
                    break;
                }
                case FHIR: {
                    ConversionReporter.setCqlConversionErrorMessage("CQl-Fir conversion produced " + matCqlConversionExceptions.size()
                            + " matCqlConversionExceptions (errorExceptions) errors.", matLibraryId);
                    ConversionReporter.setFhirMatCqlConversionExceptions(matCqlConversionExceptions, matLibraryId);

                    break;
                }
                default:
                    throw new IllegalArgumentException("Did not handle conversionType: " + conversionType);
            }
        }
    }
}
