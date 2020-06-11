package gov.cms.mat.fhir.services.components.mat;

import gov.cms.mat.cql.dto.CqlConversionPayload;
import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.commons.objects.FhirResourceValidationResult;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.components.xml.MatXmlProcessor;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.exceptions.HapiResourceValidationException;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.repository.CqlLibraryRepository;
import gov.cms.mat.fhir.services.rest.support.CqlVersionConverter;
import gov.cms.mat.fhir.services.rest.support.FhirValidatorProcessor;
import gov.cms.mat.fhir.services.service.CQLLibraryTranslationService;
import gov.cms.mat.fhir.services.service.orchestration.LibraryOrchestrationValidationService;
import gov.cms.mat.fhir.services.translate.LibraryTranslator;
import gov.cms.mat.fhir.services.translate.creators.FhirLibraryHelper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.hl7.fhir.r4.model.Library;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Slf4j
public class DraftMeasureXmlProcessor implements FhirLibraryHelper, CqlVersionConverter, FhirValidatorProcessor {
    private final HapiFhirServer hapiFhirServer;
    private final MatXmlProcessor matXmlProcessor;
    private final MatXpath matXpath;
    private final LibraryOrchestrationValidationService libraryOrchestrationValidationService;
    private final CQLLibraryTranslationService cqlLibraryTranslationService;
    private final LibraryTranslator libraryTranslator;
    private final CqlLibraryRepository cqlLibRepo;

    public DraftMeasureXmlProcessor(HapiFhirServer hapiFhirServer,
                                    MatXmlProcessor matXmlProcessor,
                                    MatXpath matXpath,
                                    LibraryOrchestrationValidationService libraryOrchestrationValidationService,
                                    CQLLibraryTranslationService cqlLibraryTranslationService,
                                    LibraryTranslator libraryTranslator,
                                    CqlLibraryRepository cqlLibRepo) {
        this.hapiFhirServer = hapiFhirServer;
        this.matXmlProcessor = matXmlProcessor;
        this.matXpath = matXpath;
        this.libraryOrchestrationValidationService = libraryOrchestrationValidationService;
        this.cqlLibraryTranslationService = cqlLibraryTranslationService;
        this.libraryTranslator = libraryTranslator;
        this.cqlLibRepo = cqlLibRepo;
    }

    public String pushStandAlone(String libraryId, String xml) {
        DraftMeasureXmlProcessor.XmlKey xmlKey = getXmlKey(xml);
        Library library = buildLibraryFromXml(libraryId, xmlKey, xml, Boolean.FALSE);
        library.setId(libraryId);
        library.setVersion(xmlKey.getVersion());

        FhirResourceValidationResult result = validateResource(library, hapiFhirServer.getCtx());

        if (CollectionUtils.isEmpty(result.getValidationErrorList())) {
            return hapiFhirServer.persist(library);
        } else {
            throw new HapiResourceValidationException(libraryId, "Library");
        }
    }

    public void processMeasure(Measure measure, boolean showWarnings) {
        String cqlLookUpXml = getMeasureXml(measure);
        XmlKey xmlKey = getXmlKey(cqlLookUpXml);

        CqlLibrary cqlLibs = cqlLibRepo.getCqlLibraryByMeasureId(measure.getId());
        if (cqlLibs == null) {
            throw new RuntimeException("Found more than 1 cql library for measure " + measure.getId());
        } else {
            Library fhirLibrary = buildLibraryFromXml(cqlLibs.getId(), xmlKey, cqlLookUpXml, showWarnings);
            libraryOrchestrationValidationService.validateFhirLibrary(xmlKey.create(),
                    measure.getId(),
                    fhirLibrary,
                    new AtomicBoolean());
        }
    }

    public XmlKey getXmlKey(String cqlLookUpXml) {
        String library = matXpath.processXmlValue(cqlLookUpXml, "library");
        String version = matXpath.processXmlValue(cqlLookUpXml, "version");
        return new XmlKey(library, version);
    }

    public String getMeasureXml(Measure measure) {
        String measureXml = findMeasureXml(measure);
        return matXpath.toQualityData(measureXml);
    }

    public Library buildLibraryFromXml(String libId, XmlKey key, String cqlLookUpXml, boolean showWarnings) {
        String cql = convertXmlToCql(key, cqlLookUpXml, showWarnings);
        CqlConversionPayload payload = convertCqlToJson(key, cql, showWarnings);

        return libraryTranslator.translateToFhir(libId, cql, payload.getXml(), payload.getJson());
    }

    private String convertXmlToCql(XmlKey key, String cqlLookUpXml, boolean showWarnings) {
        String cql = cqlLibraryTranslationService.convertMatXmlToCql(cqlLookUpXml, key.create(), showWarnings);
        ConversionReporter.setFhirCql(cql, key.create());

        return cql;
    }

    private CqlConversionPayload convertCqlToJson(XmlKey key, String cql, boolean showWarnings) {
        CqlConversionPayload payload = cqlLibraryTranslationService.convertCqlToJson(key.create(),
                new AtomicBoolean(),
                cql,
                CQLLibraryTranslationService.ConversionType.FHIR,
                showWarnings);

        ConversionReporter.setFhirElmJson(payload.getJson(), key.create());
        ConversionReporter.setFhirElmXml(payload.getXml(), key.create());

        return payload;
    }

    private String findMeasureXml(Measure measure) {
        return new String(matXmlProcessor.getXml(measure, XmlSource.MEASURE));
    }

    @Getter
    public static class XmlKey {
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
