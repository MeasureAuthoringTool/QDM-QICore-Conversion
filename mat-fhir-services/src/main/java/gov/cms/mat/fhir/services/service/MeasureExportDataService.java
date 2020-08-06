package gov.cms.mat.fhir.services.service;


import gov.cms.mat.fhir.commons.model.MeasureExport;
import gov.cms.mat.fhir.services.components.reporting.ConversionReporter;
import gov.cms.mat.fhir.services.exceptions.MeasureExportNotFoundException;
import gov.cms.mat.fhir.services.repository.MeasureExportRepository;
import gov.cms.mat.fhir.services.summary.MeasureVersionExportId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static gov.cms.mat.fhir.rest.dto.ConversionOutcome.MEASURE_EXPORT_NOT_FOUND;

@Service
@Slf4j
public class MeasureExportDataService {
    private final MeasureExportRepository measureExportRepository;

    @Value("#{'${measures.allowed.versions}'.split(',')}")
    private List<String> allowedVersions;


    public MeasureExportDataService(MeasureExportRepository measureExportRepository) {
        this.measureExportRepository = measureExportRepository;
    }

    public List<MeasureVersionExportId> getAllExportIdsAndVersion() {
        return measureExportRepository.getAllExportIdsAndVersion(allowedVersions);
    }

    public Optional<MeasureExport> findById(String id) {
        return measureExportRepository.findById(id);
    }

    public MeasureExport findByIdRequired(String id) {
        Optional<MeasureExport> optional = measureExportRepository.findByMeasureId(id);

        return optional.orElseThrow(() -> processException(id)); // ad error to conversion here
    }

    private MeasureExportNotFoundException processException(String id) {
        MeasureExportNotFoundException e = new MeasureExportNotFoundException(id);

        ConversionReporter.setTerminalMessage(e.getMessage(), MEASURE_EXPORT_NOT_FOUND);

        return e;
    }
}
