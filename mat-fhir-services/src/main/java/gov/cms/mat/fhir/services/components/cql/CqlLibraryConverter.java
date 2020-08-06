package gov.cms.mat.fhir.services.components.cql;

import gov.cms.mat.cql.exceptions.QdmMappingException;
import gov.cms.mat.fhir.rest.dto.ConversionOutcome;
import gov.cms.mat.fhir.services.components.reporting.ConversionReporter;
import gov.cms.mat.fhir.services.config.ConversionLibraryLookup;
import gov.cms.mat.fhir.services.cql.QdmCqlToFhirCqlConverter;
import gov.cms.mat.fhir.services.cql.parser.ConversionParserListener;
import gov.cms.mat.fhir.services.exceptions.CqlConversionException;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.service.CodeSystemConversionDataService;
import gov.cms.mat.fhir.services.service.QdmQiCoreDataService;
import org.springframework.stereotype.Component;

@Component
public class CqlLibraryConverter {
    private final QdmQiCoreDataService qdmQiCoreDataService;
    private final ConversionLibraryLookup conversionLibraryLookup;
    private final CodeSystemConversionDataService codeSystemConversionDataService;
    private final HapiFhirServer hapiFhirServer;

    private final ConversionParserListener conversionParserListener;

    public CqlLibraryConverter(QdmQiCoreDataService qdmQiCoreDataService,
                               ConversionLibraryLookup conversionLibraryLookup,
                               CodeSystemConversionDataService codeSystemConversionDataService,
                               HapiFhirServer hapiFhirServer,
                               ConversionParserListener conversionParserListener) {
        this.qdmQiCoreDataService = qdmQiCoreDataService;
        this.conversionLibraryLookup = conversionLibraryLookup;
        this.codeSystemConversionDataService = codeSystemConversionDataService;
        this.hapiFhirServer = hapiFhirServer;
        this.conversionParserListener = conversionParserListener;
    }

    public String convert(String cqlText, boolean includeStdLibraries) {
        try {
            QdmCqlToFhirCqlConverter qdmCqlToFhirCql = new QdmCqlToFhirCqlConverter(cqlText,
                    includeStdLibraries,
                    qdmQiCoreDataService,
                    conversionLibraryLookup.getMap(),
                    codeSystemConversionDataService.getCodeSystemMappings(),
                    hapiFhirServer);

           String cql =  qdmCqlToFhirCql.convert(null);

           return conversionParserListener.convert(cql);

        } catch (QdmMappingException e) {
            ConversionReporter.setTerminalMessage(e.getMessage(), ConversionOutcome.QDM_MAPPING_ERROR);
            throw e;
        } catch (Exception e) {
            ConversionReporter.setTerminalMessage(e.getMessage(), ConversionOutcome.LIBRARY_FHIR_CONVERSION_FAILED);
            throw new CqlConversionException("Cannot convert QDM lib to FHIR", e);
        }
    }
}
