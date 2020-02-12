package gov.cms.mat.fhir.services.components.cql;

import gov.cms.mat.fhir.rest.dto.ConversionOutcome;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.cql.QdmCqlToFhirCql;
import gov.cms.mat.fhir.services.exceptions.CqlConversionException;
import org.springframework.stereotype.Component;

@Component
public class CqlLibraryConverter {

    public String convert(String cqlText) {
        try {
            QdmCqlToFhirCql qdmCqlToFhirCql = new QdmCqlToFhirCql(cqlText);
            return qdmCqlToFhirCql.processCQL();
        } catch (Exception e) {
            ConversionReporter.setTerminalMessage(e.getMessage(), ConversionOutcome.LIBRARY_FHIR_CONVERSION_FAILED);
            throw new CqlConversionException("Cannot convert QDM lib to FHIR", e);
        }
    }
}
