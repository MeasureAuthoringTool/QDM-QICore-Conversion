package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.commons.model.MeasureExport;
import gov.cms.mat.fhir.commons.objects.TranslationOutcome;
import gov.cms.mat.fhir.services.repository.MeasureExportRepository;
import gov.cms.mat.fhir.services.repository.MeasureRepository;
import gov.cms.mat.fhir.services.summary.MeasureVersionExportId;
import gov.cms.mat.fhir.services.translate.ValueSetMapper;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    private static final List<String> ALLOWED_VERSIONS = Arrays.asList("v5.5", "v5.6", "v5.7", "v5.8");
    private static final String TRANSLATE_SUCCESS_MESSAGE = "Read %d Measure Export objects converted %d Value sets to fhir in %d seconds";

    private final MeasureRepository measureRepository;
    private final MeasureExportRepository measureExportRepository;
    private final ValueSetMapper valueSetMapper;

    public ValueSetController(MeasureRepository measureRepository, MeasureExportRepository measureExportRepository, ValueSetMapper valueSetMapper) {
        this.measureRepository = measureRepository;
        this.measureExportRepository = measureExportRepository;
        this.valueSetMapper = valueSetMapper;
    }

    @Transactional(readOnly = true)
    @GetMapping(path = "/translateAll")
    public TranslationOutcome translateAll() {
        Instant startTime = Instant.now();
        int startCount = valueSetMapper.count();

        int measureExportCount = processMeasureExport();

        int finishCount = valueSetMapper.count();
        long duration = Duration.between(startTime, Instant.now()).toMillis() / 1000;
        String successMessage =
                String.format(TRANSLATE_SUCCESS_MESSAGE, measureExportCount, finishCount - startCount, duration);
        log.info(successMessage);

        return createOutcome(successMessage);
    }

    private int processMeasureExport() {
        List<ValueSet> outcomes = new ArrayList<>();

        List<MeasureVersionExportId> idsAndVersion = measureExportRepository.getAllExportIdsAndVersion(ALLOWED_VERSIONS);
        int measureExportCount = idsAndVersion.size();

        idsAndVersion.stream()
                .peek(mv -> log.debug("Processing  measureExport: {}", mv.toString()))
                .map(mv -> measureExportRepository.findById(mv.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(me -> translate(me, outcomes));

        return measureExportCount;
    }

    private TranslationOutcome createOutcome(String successMessage) {
        TranslationOutcome res = new TranslationOutcome();
        res.setMessage(successMessage);
        res.setSuccessful(true);
        return res;
    }

    @GetMapping(path = "/count")
    public int countValueSets() {
        return valueSetMapper.count();
    }

    @DeleteMapping(path = "/deleteAll")
    public int deleteValueSets() {
        return valueSetMapper.deleteAll();
    }

    private void translate(MeasureExport measureExport, List<ValueSet> outcomes) {
        List<ValueSet> valueSets = valueSetMapper.translateToFhir(new String(measureExport.getSimpleXml()));
        outcomes.addAll(valueSets);
    }
}