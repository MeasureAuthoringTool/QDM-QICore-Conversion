package gov.cms.mat.fhir.services.service;

import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.commons.model.MeasureExport;
import gov.cms.mat.fhir.rest.dto.ConversionType;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.components.mongo.ConversionResultsService;
import gov.cms.mat.fhir.services.components.xml.MatXmlProcessor;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.exceptions.ValueSetConversionException;
import gov.cms.mat.fhir.services.summary.MeasureVersionExportId;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import gov.cms.mat.fhir.services.translate.ValueSetMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ValueSetService {
    private static final String TRANSLATE_SUCCESS_MESSAGE = "Read %d Measure Export objects converted %d " +
            "Value sets to fhir in %d seconds";

    private static final String XML_NOT_FOUND_MESSAGE = "Cannot find XML with MeasureId: {} and XmlSource: {}";


    private final MeasureExportDataService measureExportDataService;
    private final ValueSetMapper valueSetMapper;
    private final MatXmlProcessor matXmlProcessor;
    private final ConversionResultsService conversionResultsService;
    private final MeasureDataService measureDataService;

    public ValueSetService(MeasureExportDataService measureExportDataService,
                           ValueSetMapper valueSetMapper,
                           MatXmlProcessor matXmlProcessor, ConversionResultsService conversionResultsService, MeasureDataService measureDataService) {
        this.measureExportDataService = measureExportDataService;
        this.valueSetMapper = valueSetMapper;
        this.matXmlProcessor = matXmlProcessor;
        this.conversionResultsService = conversionResultsService;
        this.measureDataService = measureDataService;
    }

    public int count() {
        return valueSetMapper.count();
    }

    public int deleteAll() {
        return valueSetMapper.deleteAll();
    }

    public List<ValueSet> findValueSetsByMeasureId(XmlSource xmlSource, String measureId, ConversionType conversionType) {
        Measure matMeasure = measureDataService.findOneValid(measureId); // if not valid will throw

        OrchestrationProperties properties = OrchestrationProperties.builder()
                .matMeasure(matMeasure)
                .xmlSource(xmlSource)
                .conversionType(conversionType)
                .build();


        return findValueSetsByMeasure(properties);
    }

    public List<ValueSet> findValueSetsByMeasure(OrchestrationProperties properties) {
        Measure matMeasure = properties.getMatMeasure();

        byte[] xml = getXmlBytesBySource(matMeasure.getId(), properties.getXmlSource());

        if (ArrayUtils.isEmpty(xml)) {
            log.warn(XML_NOT_FOUND_MESSAGE, matMeasure.getId(), properties.getXmlSource());
            throw new ValueSetConversionException("No value sets found for measure id: " + matMeasure.getId());
        } else {
            return translateToFhir(matMeasure.getId(), xml);
        }
    }

    public String translateAll(XmlSource xmlSource, ConversionType conversionType) {
        Instant startTime = Instant.now();
        int startCount = valueSetMapper.count();

        int measureExportCount = processValueSets(xmlSource);

        int finishCount = valueSetMapper.count();
        long duration = Duration.between(startTime, Instant.now()).toMillis() / 1000;

        return String.format(TRANSLATE_SUCCESS_MESSAGE, measureExportCount, finishCount - startCount, duration);
    }

    private int processValueSets(XmlSource xmlSource) {
        List<ValueSet> outcomes = new ArrayList<>();

        List<MeasureVersionExportId> idsAndVersion = measureExportDataService.getAllExportIdsAndVersion();
        int measureExportCount = idsAndVersion.size();

        idsAndVersion.stream()
                .map(mv -> measureExportDataService.findById(mv.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(me -> translate(me, outcomes, xmlSource));

        return measureExportCount;
    }

    private void translate(MeasureExport measureExport,
                           List<ValueSet> outcomes,
                           XmlSource xmlSource) {
        byte[] xmlBytes = getXmlBytesBySource(measureExport.getMeasureId(), xmlSource);

        if (xmlBytes == null) {
            log.warn(XML_NOT_FOUND_MESSAGE, measureExport.getMeasureId(), xmlSource);
        } else {
            List<ValueSet> valueSets = translateToFhir(measureExport.getMeasureId(), xmlBytes);
            outcomes.addAll(valueSets);
        }
    }

    private List<ValueSet> translateToFhir(String measureId,
                                           byte[] xmlBytes) {
        ConversionReporter.setInThreadLocal(measureId, conversionResultsService);

        return valueSetMapper.translateToFhir(new String(xmlBytes));
    }

    private byte[] getXmlBytesBySource(String measureId, XmlSource xmlSource) {
        return matXmlProcessor.getXmlById(measureId, xmlSource);
    }
}
