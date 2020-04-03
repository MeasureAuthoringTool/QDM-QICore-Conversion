package gov.cms.mat.fhir.services.components.mat;

import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.components.xml.MatXmlProcessor;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.rest.support.CqlVersionConverter;
import gov.cms.mat.fhir.services.service.CQLLibraryTranslationService;
import gov.cms.mat.fhir.services.service.orchestration.LibraryOrchestrationValidationService;
import gov.cms.mat.fhir.services.translate.XmlLibraryTranslator;
import gov.cms.mat.fhir.services.translate.creators.FhirLibraryHelper;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Library;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Slf4j
public class DraftMeasureXmlProcessor implements FhirLibraryHelper, CqlVersionConverter {
    private final HapiFhirServer hapiFhirServer;
    private final MatXmlProcessor matXmlProcessor;
    private final MatXpath matXpath;
    private final LibraryOrchestrationValidationService libraryOrchestrationValidationService;
    private final CQLLibraryTranslationService cqlLibraryTranslationService;

    public DraftMeasureXmlProcessor(HapiFhirServer hapiFhirServer,
                                    MatXmlProcessor matXmlProcessor,
                                    MatXpath matXpath,
                                    LibraryOrchestrationValidationService libraryOrchestrationValidationService,
                                    CQLLibraryTranslationService cqlLibraryTranslationService) {
        this.hapiFhirServer = hapiFhirServer;
        this.matXmlProcessor = matXmlProcessor;
        this.matXpath = matXpath;
        this.libraryOrchestrationValidationService = libraryOrchestrationValidationService;
        this.cqlLibraryTranslationService = cqlLibraryTranslationService;
    }

    public void process(Measure measure, boolean showWarnings) {
        String cqlLookUpXml = getMeasureXml(measure);
        String library = matXpath.processXmlValue(cqlLookUpXml, "library");
        String version = matXpath.processXmlValue(cqlLookUpXml, "version");
        XmlKey xmlKey = new XmlKey(library, version);

        Library fhirLibrary = getLibrary(xmlKey, cqlLookUpXml, showWarnings);

        libraryOrchestrationValidationService.validateFhirLibrary(xmlKey.create(),
                measure.getId(),
                fhirLibrary,
                new AtomicBoolean());
    }

    public String getMeasureXml(Measure measure) {
        String measureXml = findMeasureXml(measure);
        return matXpath.toQualityData(measureXml);
    }

    private Library getLibrary(XmlKey key, String cqlLookUpXml, boolean showWarnings) {
        String cql = getCql(key, cqlLookUpXml, showWarnings);

        String json = getJson(key, cql, showWarnings);

        Library library = getLibrary(key, cql, json);
        ConversionReporter.setFhirJson(hapiFhirServer.toJson(library), key.create());
        return library;
    }

    private String getCql(XmlKey key, String cqlLookUpXml, boolean showWarnings) {
        String cql = cqlLibraryTranslationService.convertMatXmlToCql(cqlLookUpXml, key.create(), showWarnings);
        ConversionReporter.setFhirCql(cql, key.create());
        return cql;
    }

    private Library getLibrary(XmlKey key, String cql, String json) {
        BigDecimal versionBigDecimal = convertVersionToBigDecimal(key.version);

        XmlLibraryTranslator xmlLibraryTranslator = new XmlLibraryTranslator(key.library,
                cql,
                json,
                hapiFhirServer.getBaseURL(),
                UUID.randomUUID().toString());

        return xmlLibraryTranslator.translateToFhir(versionBigDecimal.toString());
    }

    private String getJson(XmlKey key, String cql, boolean showWarnings) {
        String json = cqlLibraryTranslationService.convertCqlToJson(key.create(),
                new AtomicBoolean(),
                cql,
                CQLLibraryTranslationService.ConversionType.FHIR,
                showWarnings);

        ConversionReporter.setFhirJson(json, key.create());

        return json;
    }

    private String findMeasureXml(Measure measure) {
        return new String(matXmlProcessor.getXml(measure, XmlSource.MEASURE));
    }

    private static class XmlKey {
        final String library;
        final String version;

        XmlKey(String library, String version) {
            this.library = library;
            this.version = version;
        }

        public String create() {
            return library + "-" + version;
        }
    }
}
