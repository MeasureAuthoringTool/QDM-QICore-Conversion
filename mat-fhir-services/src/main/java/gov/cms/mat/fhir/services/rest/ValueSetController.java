package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.commons.model.MeasureExport;
import gov.cms.mat.fhir.services.repository.MeasureExportRepository;
import gov.cms.mat.fhir.services.repository.MeasureRepository;
import gov.cms.mat.fhir.services.summary.MeasureVersionExportId;
import gov.cms.mat.fhir.services.translate.ValueSetMapper;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/valueSet")
@Slf4j
public class ValueSetController {
    private final static List<String> ALLOWED_VERSIONS = Arrays.asList("v5.5", "v5.6", "v5.7", "v5.8");


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
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<ValueSet> translateAll() {
        List<ValueSet> outcomes = new ArrayList<>();

        List<MeasureVersionExportId> idsAndVersion = measureExportRepository.getAllExportIdsAndVersion(ALLOWED_VERSIONS);

        idsAndVersion.stream()
                .peek(mv -> log.debug(mv.toString()))
                .map(mv -> measureExportRepository.findById(mv.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(me -> translate(me, outcomes));

        return outcomes;
    }

    private void translate(MeasureExport measureExport, List<ValueSet> outcomes) {
        if (outcomes.size() > 10) {
            return;
        }

        List<ValueSet> valueSets = valueSetMapper.translateToFhir(new String(measureExport.getSimpleXml()));
        outcomes.addAll(valueSets);
    }


}