package gov.cms.mat.fhir.services.service;

import gov.cms.mat.fhir.commons.model.MeasureExport;
import gov.cms.mat.fhir.rest.cql.ConversionType;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.components.mongo.ConversionResultsService;
import gov.cms.mat.fhir.services.components.xml.MatXmlProcessor;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.summary.MeasureVersionExportId;
import gov.cms.mat.fhir.services.translate.ValueSetMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
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

    public List<ValueSet> findValueSets(XmlSource xmlSource, String measureId, ConversionType conversionType) {
        measureDataService.findOneValid(measureId); // if not valid will throw

        byte[] xml = getXmlBytesBySource(measureId, xmlSource);

        if (ArrayUtils.isEmpty(xml)) {
            log.warn(XML_NOT_FOUND_MESSAGE, measureId, xmlSource);
            return Collections.emptyList();
        } else {
            return translateToFhir(measureId, xml, conversionType);
        }
    }

    public String translateAll(XmlSource xmlSource, ConversionType conversionType) {
        Instant startTime = Instant.now();
        int startCount = valueSetMapper.count();

        int measureExportCount = processValueSets(xmlSource, conversionType);

        int finishCount = valueSetMapper.count();
        long duration = Duration.between(startTime, Instant.now()).toMillis() / 1000;

        return String.format(TRANSLATE_SUCCESS_MESSAGE, measureExportCount, finishCount - startCount, duration);
    }

    private int processValueSets(XmlSource xmlSource, ConversionType conversionType) {
        List<ValueSet> outcomes = new ArrayList<>();

        List<MeasureVersionExportId> idsAndVersion = measureExportDataService.getAllExportIdsAndVersion();
        int measureExportCount = idsAndVersion.size();

        idsAndVersion.stream()
                .map(mv -> measureExportDataService.findById(mv.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(me -> translate(me, outcomes, xmlSource, conversionType));

        return measureExportCount;
    }

    private void translate(MeasureExport measureExport,
                           List<ValueSet> outcomes,
                           XmlSource xmlSource,
                           ConversionType conversionType) {
        byte[] xmlBytes = getXmlBytesBySource(measureExport.getMeasureId(), xmlSource);

        if (xmlBytes == null) {
            log.warn(XML_NOT_FOUND_MESSAGE, measureExport.getMeasureId(), xmlSource);
        } else {
            List<ValueSet> valueSets = translateToFhir(measureExport.getMeasureId(), xmlBytes, conversionType);
            outcomes.addAll(valueSets);
        }
    }

    private List<ValueSet> translateToFhir(String measureId,
                                           byte[] xmlBytes,
                                           ConversionType conversionType) {
        ConversionReporter.setInThreadLocal(measureId, conversionResultsService);

        return valueSetMapper.translateToFhir(new String(xmlBytes), conversionType);
    }

    private byte[] getXmlBytesBySource(String measureId, XmlSource xmlSource) {
        return matXmlProcessor.getXmlById(measureId, xmlSource);
    }
}
