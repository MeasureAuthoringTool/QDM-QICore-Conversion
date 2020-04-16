package gov.cms.mat.fhir.services.components.cql;

import gov.cms.mat.cql.exceptions.QdmMappingException;
import gov.cms.mat.fhir.rest.dto.ConversionOutcome;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.config.ConversionLibraryLookup;
import gov.cms.mat.fhir.services.cql.QdmCqlToFhirCqlConverter;
import gov.cms.mat.fhir.services.exceptions.CqlConversionException;
import gov.cms.mat.fhir.services.service.QdmQiCoreDataService;
import org.springframework.stereotype.Component;

@Component
public class CqlLibraryConverter {
    private final QdmQiCoreDataService qdmQiCoreDataService;
    private final ConversionLibraryLookup conversionLibraryLookup;

    public CqlLibraryConverter(QdmQiCoreDataService qdmQiCoreDataService,
                               ConversionLibraryLookup conversionLibraryLookup) {
        this.qdmQiCoreDataService = qdmQiCoreDataService;
        this.conversionLibraryLookup = conversionLibraryLookup;
    }

    public String convert(String cqlText) {
        try {
            QdmCqlToFhirCqlConverter qdmCqlToFhirCql = new QdmCqlToFhirCqlConverter(cqlText,
                    qdmQiCoreDataService,
                    conversionLibraryLookup.getMap());
            return qdmCqlToFhirCql.convert(null);
        } catch (QdmMappingException e) {
            ConversionReporter.setTerminalMessage(e.getMessage(), ConversionOutcome.QDM_MAPPING_ERROR);
            throw e;
        } catch (Exception e) {
            ConversionReporter.setTerminalMessage(e.getMessage(), ConversionOutcome.LIBRARY_FHIR_CONVERSION_FAILED);
            throw new CqlConversionException("Cannot convert QDM lib to FHIR", e);
        }
    }
}
