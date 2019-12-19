package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.commons.model.MeasureExport;
import gov.cms.mat.fhir.commons.objects.FhirResourceValidationResult;
import gov.cms.mat.fhir.commons.objects.TranslationOutcome;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.components.mongo.ConversionResult;
import gov.cms.mat.fhir.services.components.mongo.ConversionResultsService;
import gov.cms.mat.fhir.services.components.mongo.ConversionType;
import gov.cms.mat.fhir.services.components.xml.MatXmlProcessor;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.exceptions.ValueSetConversionException;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.rest.support.FhirValidatorProcessor;
import gov.cms.mat.fhir.services.service.MeasureDataService;
import gov.cms.mat.fhir.services.service.MeasureExportDataService;
import gov.cms.mat.fhir.services.summary.FhirValueSetResourceValidationResult;
import gov.cms.mat.fhir.services.summary.MeasureVersionExportId;
import gov.cms.mat.fhir.services.translate.ValueSetMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/valueSet")
@Tag(name = "ValueSet-Controller", description = "API for converting MAT ValueSets to FHIR.")
@Slf4j
public class ValueSetController implements FhirValidatorProcessor {

    private static final String TRANSLATE_SUCCESS_MESSAGE = "Read %d Measure Export objects converted %d " +
            "Value sets to fhir in %d seconds";

    private final MeasureExportDataService measureExportDataService;
    private final ValueSetMapper valueSetMapper;
    private final ConversionResultsService conversionResultsService;
    private final MatXmlProcessor matXmlProcessor;
    private final MeasureDataService measureDataService;
    private final HapiFhirServer hapiFhirServer;


    public ValueSetController(MeasureExportDataService measureExportDataService,
                              ValueSetMapper valueSetMapper,
                              ConversionResultsService conversionResultsService,
                              MatXmlProcessor matXmlProcessor,
                              MeasureDataService measureDataService, HapiFhirServer hapiFhirServer) {
        this.measureExportDataService = measureExportDataService;
        this.valueSetMapper = valueSetMapper;
        this.conversionResultsService = conversionResultsService;
        this.matXmlProcessor = matXmlProcessor;
        this.measureDataService = measureDataService;
        this.hapiFhirServer = hapiFhirServer;
    }

    @Operation(summary = "Translate all ValueSets in MAT to FHIR.",
            description = "Translate all the ValueSets in the MAT Database and persist to the HAPI FHIR Database.")
    @Transactional(readOnly = true)
    @PutMapping(path = "/translateAll")
    public TranslationOutcome translateAll(
            @RequestParam(required = false, defaultValue = "SIMPLE") XmlSource xmlSource,
            @RequestParam(required = false, defaultValue = "CONVERSION") ConversionType conversionType) {

        Instant startTime = Instant.now();
        int startCount = valueSetMapper.count();

        int measureExportCount = processMeasureExport(xmlSource, conversionType);

        int finishCount = valueSetMapper.count();
        long duration = Duration.between(startTime, Instant.now()).toMillis() / 1000;
        String successMessage =
                String.format(TRANSLATE_SUCCESS_MESSAGE, measureExportCount, finishCount - startCount, duration);
        log.info(successMessage);

        return createOutcome(successMessage);
    }

    @Operation(summary = "Validate ValueSet conversion MAT to FHIR.",
            description = "Validate ValueSet conversion MAT to FHIR of all the ValueSets that are contained in the " +
                    "measure indicated by measureId.")
    @Transactional(readOnly = true)
    @PutMapping(path = "/validate")
    public FhirValueSetResourceValidationResult validate(
            @RequestParam(required = false, defaultValue = "SIMPLE") XmlSource xmlSource,
            @RequestParam String measureId) {

        measureDataService.findOneValid(measureId);

        byte[] xml = getXmlBytesBySource(measureId, xmlSource);

        List<ValueSet> valueSets = translateToFhir(measureId, xml, ConversionType.VALIDATION);

        if (valueSets.isEmpty()) {
            throw new ValueSetConversionException("No value sets found");
        } else {
            return generateValidationResults(valueSets, xmlSource, measureId);
        }
    }

    public FhirValueSetResourceValidationResult generateValidationResults(List<ValueSet> valueSets,
                                                                          XmlSource xmlSource,
                                                                          String measureId) {

        FhirValueSetResourceValidationResult response = new FhirValueSetResourceValidationResult();

        List<FhirResourceValidationResult> results = valueSets.stream()
                .map(v -> createResult(v, measureId))
                .collect(Collectors.toList());
        response.setFhirResourceValidationResults(results);

        ConversionResult conversionResult = ConversionReporter.getConversionResult();

        response.setValueSetConversionType(conversionResult.getValueSetConversionType());
        response.setValueSetResults(conversionResult.getValueSetResults());
        response.setXmlSource(xmlSource);

        return response;
    }

    private FhirResourceValidationResult createResult(ValueSet valueSet, String measureId) {
        FhirResourceValidationResult res = new FhirResourceValidationResult();
        validateResource(res, valueSet, hapiFhirServer.getCtx());

        res.setId(valueSet.getId());
        res.setType("ValueSet");
        res.setMeasureId(measureId);

        List<ConversionResult.FhirValidationResult> results = buildResultList(res);
        ConversionReporter.setValueSetsValidationResults(res.getId(), results);

        return res;
    }

    private List<ConversionResult.FhirValidationResult> buildResultList(FhirResourceValidationResult res) {
        if (CollectionUtils.isEmpty(res.getValidationErrorList())) {
            return Collections.emptyList();
        } else {
            return buildResults(res);
        }
    }

    @Operation(summary = "Count of persisted FHIR ValueSets.",
            description = "The count of all the ValueSets in the HAPI FHIR Database.")
    @GetMapping(path = "/count")
    public int countValueSets() {
        return valueSetMapper.count();
    }

    @Operation(summary = "Delete all persisted FHIR ValueSets.",
            description = "Delete all the ValueSets in the HAPI FHIR Database.")
    @DeleteMapping(path = "/deleteAll")
    public int deleteValueSets() {
        return valueSetMapper.deleteAll();
    }

    private int processMeasureExport(XmlSource xmlSource, ConversionType conversionType) {
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

    private TranslationOutcome createOutcome(String successMessage) {
        TranslationOutcome res = new TranslationOutcome();
        res.setMessage(successMessage);
        res.setSuccessful(true);
        return res;
    }

    private void translate(MeasureExport measureExport,
                           List<ValueSet> outcomes,
                           XmlSource xmlSource,
                           ConversionType conversionType) {
        byte[] xmlBytes = getXmlBytesBySource(measureExport.getMeasureId(), xmlSource);

        if (xmlBytes == null) {
            log.warn("Cannot find XML with MeasureId: {} and XmlSource: {}", measureExport.getMeasureId(), xmlSource);
        } else {
            List<ValueSet> valueSets = translateToFhir(measureExport.getMeasureId(), xmlBytes, conversionType);
            outcomes.addAll(valueSets);
        }
    }

    private byte[] getXmlBytesBySource(String measureId, XmlSource xmlSource) {
        return matXmlProcessor.getXmlById(measureId, xmlSource);
    }

    private List<ValueSet> translateToFhir(String measureId,
                                           byte[] xmlBytes,
                                           ConversionType conversionType) {
        if (xmlBytes == null) {
            log.warn("Cannot find XML with MeasureId: {}", measureId);
            throw new ValueSetConversionException("Cannot find XML with MeasureId: " + measureId);
        }

        ConversionReporter.setInThreadLocal(measureId, conversionResultsService);
        ConversionReporter.resetValueSetResults(conversionType);

        return valueSetMapper.translateToFhir(new String(xmlBytes), conversionType);
    }
}