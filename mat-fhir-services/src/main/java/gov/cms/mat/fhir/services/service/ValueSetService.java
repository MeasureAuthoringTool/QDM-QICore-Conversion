package gov.cms.mat.fhir.services.service;

import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.rest.dto.ConversionOutcome;
import gov.cms.mat.fhir.rest.dto.ConversionType;
import gov.cms.mat.fhir.services.components.reporting.ConversionReporter;
import gov.cms.mat.fhir.services.components.xml.MatXmlProcessor;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.exceptions.ValueSetConversionException;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import gov.cms.mat.fhir.services.translate.ValueSetMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ValueSetService {
    private static final String XML_NOT_FOUND_MESSAGE = "Cannot find XML with MeasureId: %s and XmlSource: %s";

    private final ValueSetMapper valueSetMapper;
    private final MatXmlProcessor matXmlProcessor;
    private final MeasureDataService measureDataService;

    public ValueSetService(ValueSetMapper valueSetMapper,
                           MatXmlProcessor matXmlProcessor,
                           MeasureDataService measureDataService) {
        this.valueSetMapper = valueSetMapper;
        this.matXmlProcessor = matXmlProcessor;
        this.measureDataService = measureDataService;
    }

    public int count() {
        return valueSetMapper.count();
    }

    public int deleteAll() {
        return valueSetMapper.deleteAll();
    }

    public List<ValueSet> findValueSetsByMeasureId(XmlSource xmlSource, String measureId, ConversionType conversionType, String vsacGrantingTicket) {
        var matMeasure = measureDataService.findOneValid(measureId); // if not valid will throw

        var properties = OrchestrationProperties.builder()
                .matMeasure(matMeasure)
                .xmlSource(xmlSource)
                .conversionType(conversionType)
                .vsacGrantingTicket(vsacGrantingTicket)
                .build();

        return findValueSetsByMeasure(properties);
    }

    public List<ValueSet> findValueSetsByMeasure(OrchestrationProperties properties) {
        Measure matMeasure = properties.getMatMeasure();

        byte[] xml = getXmlBytesBySource(matMeasure.getId(), properties.getXmlSource());

        if (ArrayUtils.isEmpty(xml)) {
            String message = String.format(XML_NOT_FOUND_MESSAGE, matMeasure.getId(), properties.getXmlSource());
            log.warn(message);
            ConversionReporter.setTerminalMessage(message, ConversionOutcome.MEASURE_XML_NOT_FOUND);
            throw new ValueSetConversionException(message);
        } else {
            return valueSetMapper.translateToFhir(new String(xml), properties.getVsacGrantingTicket());
        }
    }

    private byte[] getXmlBytesBySource(String measureId, XmlSource xmlSource) {
        return matXmlProcessor.getXmlById(measureId, xmlSource);
    }
}
