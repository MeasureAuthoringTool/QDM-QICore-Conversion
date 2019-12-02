package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.commons.model.MeasureExport;
import gov.cms.mat.fhir.commons.objects.TranslationOutcome;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.components.mongo.ConversionResultsService;
import gov.cms.mat.fhir.services.components.xml.MatXmlProcessor;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.repository.MeasureExportRepository;
import gov.cms.mat.fhir.services.summary.MeasureVersionExportId;
import gov.cms.mat.fhir.services.translate.ValueSetMapper;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/valueSet")
@Slf4j
public class ValueSetController {
    static final List<String> ALLOWED_VERSIONS = Arrays.asList("v5.5", "v5.6", "v5.7", "v5.8");
    private static final XmlSource DEFAULT_XML_SOURCE = XmlSource.SIMPLE;
    private static final String TRANSLATE_SUCCESS_MESSAGE = "Read %d Measure Export objects converted %d " +
            "Value sets to fhir in %d seconds";

    private final MeasureExportRepository measureExportRepository;
    private final ValueSetMapper valueSetMapper;
    private final ConversionResultsService conversionResultsService;
    private final MatXmlProcessor matXmlProcessor;

    public ValueSetController(MeasureExportRepository measureExportRepository,
                              ValueSetMapper valueSetMapper,
                              ConversionResultsService conversionResultsService,
                              MatXmlProcessor matXmlProcessor) {
        this.measureExportRepository = measureExportRepository;
        this.valueSetMapper = valueSetMapper;
        this.conversionResultsService = conversionResultsService;
        this.matXmlProcessor = matXmlProcessor;
    }

    @Transactional(readOnly = true)
    @PutMapping(path = "/translateAll")
    public TranslationOutcome translateAll(@RequestParam(required = false) XmlSource xmlSource) {

        if (xmlSource == null) {
            xmlSource = DEFAULT_XML_SOURCE;
        }

        Instant startTime = Instant.now();
        int startCount = valueSetMapper.count();

        int measureExportCount = processMeasureExport(xmlSource);

        int finishCount = valueSetMapper.count();
        long duration = Duration.between(startTime, Instant.now()).toMillis() / 1000;
        String successMessage =
                String.format(TRANSLATE_SUCCESS_MESSAGE, measureExportCount, finishCount - startCount, duration);
        log.info(successMessage);

        return createOutcome(successMessage);
    }

    @GetMapping(path = "/count")
    public int countValueSets() {
        return valueSetMapper.count();
    }

    @DeleteMapping(path = "/deleteAll")
    public int deleteValueSets() {
        return valueSetMapper.deleteAll();
    }

    private int processMeasureExport(XmlSource xmlSource) {
        List<ValueSet> outcomes = new ArrayList<>();

        List<MeasureVersionExportId> idsAndVersion = measureExportRepository.getAllExportIdsAndVersion(ALLOWED_VERSIONS);
        int measureExportCount = idsAndVersion.size();

        idsAndVersion.stream()
                .map(mv -> measureExportRepository.findById(mv.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(me -> translate(me, outcomes, xmlSource));

        return measureExportCount;
    }

    private TranslationOutcome createOutcome(String successMessage) {
        TranslationOutcome res = new TranslationOutcome();
        res.setMessage(successMessage);
        res.setSuccessful(true);
        return res;
    }

    private void translate(MeasureExport measureExport, List<ValueSet> outcomes, XmlSource xmlSource) {
        byte[] xmlBytes = matXmlProcessor.getXmlById(measureExport.getMeasureId(), xmlSource);

        if (xmlBytes == null) {
            log.warn("Cannot find XML with MeasureId: {} and XmlSource: {}", measureExport.getMeasureId(), xmlSource);
        } else {
            translateToFhir(measureExport, outcomes, xmlBytes);
        }
    }

    private void translateToFhir(MeasureExport measureExport, List<ValueSet> outcomes, byte[] xmlBytes) {
        ConversionReporter.setInThreadLocal(measureExport.getMeasureId(), conversionResultsService);
        ConversionReporter.resetValueSetResults();

        List<ValueSet> valueSets = valueSetMapper.translateToFhir(new String(xmlBytes));
        outcomes.addAll(valueSets);

        ConversionReporter.removeInThreadLocal();
    }
}