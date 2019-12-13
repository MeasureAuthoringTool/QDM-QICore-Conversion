package gov.cms.mat.fhir.services.service;


import gov.cms.mat.fhir.commons.model.MeasureExport;
import gov.cms.mat.fhir.services.repository.MeasureExportRepository;
import gov.cms.mat.fhir.services.summary.MeasureVersionExportId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class MeasureExportService {
    private final MeasureExportRepository measureExportRepository;

    @Value("#{'${measures.allowed.versions}'.split(',')}")
    private List<String> allowedVersions;


    public MeasureExportService(MeasureExportRepository measureExportRepository) {
        this.measureExportRepository = measureExportRepository;
    }

    public List<MeasureVersionExportId> getAllExportIdsAndVersion() {
        return measureExportRepository.getAllExportIdsAndVersion(allowedVersions);
    }

    public Optional<MeasureExport> findById(String id) {
        return measureExportRepository.findById(id);
    }
}
