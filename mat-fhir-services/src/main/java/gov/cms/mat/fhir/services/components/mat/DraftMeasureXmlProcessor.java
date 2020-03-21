package gov.cms.mat.fhir.services.components.mat;

import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.components.xml.MatXmlProcessor;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.exceptions.CqlLibraryNotFoundException;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.service.CQLLibraryTranslationService;
import gov.cms.mat.fhir.services.service.orchestration.LibraryOrchestrationValidationService;
import gov.cms.mat.fhir.services.translate.creators.FhirLibraryHelper;
import lombok.extern.slf4j.Slf4j;
import mat.model.cql.CQLQualityDataModelWrapper;
import org.apache.commons.collections4.CollectionUtils;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Library;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static gov.cms.mat.fhir.services.translate.LibraryTranslatorBase.CQL_CONTENT_TYPE;
import static gov.cms.mat.fhir.services.translate.LibraryTranslatorBase.ELM_CONTENT_TYPE;

@Component
@Slf4j
public class DraftMeasureXmlProcessor implements FhirLibraryHelper {

    private final HapiFhirServer hapiFhirServer;
    private final MatXmlProcessor matXmlProcessor;

    private final MatXmlConverter matXmlConverter;

    private final MatXpath matXpath;
    private final LibraryOrchestrationValidationService libraryOrchestrationValidationService;
    private final CQLLibraryTranslationService cqlLibraryTranslationService;


    public DraftMeasureXmlProcessor(HapiFhirServer hapiFhirServer, MatXmlProcessor matXmlProcessor, MatXmlConverter matXmlConverter, MatXpath matXpath, LibraryOrchestrationValidationService libraryOrchestrationValidationService, CQLLibraryTranslationService cqlLibraryTranslationService) {
        this.hapiFhirServer = hapiFhirServer;
        this.matXmlProcessor = matXmlProcessor;
        this.matXmlConverter = matXmlConverter;
        this.matXpath = matXpath;
        this.libraryOrchestrationValidationService = libraryOrchestrationValidationService;
        this.cqlLibraryTranslationService = cqlLibraryTranslationService;
    }

    /*
    <library>matMeasure.ABBR_NAME</library>
    <version>owe you algo for version. I need it too. Will create with my work</version>
     <libraryComment/> always empty.
<usingModel>FHIR</usingModel>
<usingModelVersion>duane knows this. Its the fhir version we use in cql.</usingModelVersion>
<cqlContext>Patient</cqlContext>
     */
    public void process(Measure measure) {
        String xml = getXml(measure);
        String cqlLookUpXml = matXpath.toQualityData(xml);

        Library fhirLibrary = getLibrary(measure, cqlLookUpXml);

        processContentList(fhirLibrary.getContent(), fhirLibrary.getName());
        String fhirCql = ConversionReporter.getFhirCql(fhirLibrary.getName());

        //remove after showing carson
        CQLQualityDataModelWrapper cqlQualityDataModelWrapper = matXmlConverter.toQualityData(xml);

        libraryOrchestrationValidationService.validateFhirLibrary(fhirLibrary.getName(), measure.getId(), fhirLibrary, new AtomicBoolean());


        cqlLibraryTranslationService.convertCqlToJson(fhirLibrary.getName(), new AtomicBoolean(), fhirCql, CQLLibraryTranslationService.ConversionType.FHIR);

    }

    public Library getLibrary(Measure measure, String cqlLookUpXml) {
        String library = matXpath.processXmlValue(cqlLookUpXml, "library");
        String version = matXpath.processXmlValue(cqlLookUpXml, "version");

        Bundle bundle = hapiFhirServer.fetchLibraryBundleByVersionAndName(version, library);

        if (CollectionUtils.isEmpty(bundle.getEntry())) {
            throw new CqlLibraryNotFoundException(measure.getId());
        }

        var optional = hapiFhirServer.findResourceInBundle(bundle, Library.class);

        if (optional.isEmpty()) {
            throw new CqlLibraryNotFoundException(measure.getId());
        }

        return optional.get();
    }

    private void processContentList(List<Attachment> content, String name) {
        content.forEach(c -> processContent(c, name));
    }


    private void processContent(Attachment c, String name) {
        switch (c.getContentType()) {
            case ELM_CONTENT_TYPE:
                ConversionReporter.setFhirJson(decodeBase64(c.getData()), name);
                break;
            case CQL_CONTENT_TYPE:
                ConversionReporter.setFhirCql(decodeBase64(c.getData()), name);
                break;
            default:
                throw new IllegalArgumentException("Cannot handle mime type: " + c.getContentType());
        }

    }

    private String getXml(Measure measure) {
        return new String(matXmlProcessor.getXml(measure, XmlSource.MEASURE));
    }
}
