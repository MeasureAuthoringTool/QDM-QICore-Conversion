package gov.cms.mat.fhir.services.service;


import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.services.exceptions.MeasureNotFoundException;
import gov.cms.mat.fhir.services.exceptions.MeasureReleaseVersionInvalidException;
import gov.cms.mat.fhir.services.repository.MeasureRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class MeasureDataService {
    private final MeasureRepository measureRepository;
    @Value("#{'${measures.allowed.versions}'.split(',')}")
    private List<String> allowedVersions;

    public MeasureDataService(MeasureRepository measureRepository) {
        this.measureRepository = measureRepository;
    }

    public List<String> findAllIds() {
        return measureRepository.findAllIds();
    }

    public List<String> findAllValidIds() {
        return measureRepository.findAllIdsWithAllowedVersions(allowedVersions);
    }

    public Measure findOneValid(String measureId) {
        Optional<Measure> optional = measureRepository.findById(measureId);

        if (optional.isPresent()) {
            return checkMeasure(measureId, optional.get());
        } else {
            throw new MeasureNotFoundException(measureId);
        }
    }

    public Measure checkMeasure(String measureId, Measure measure) {
        if (!isValidReleaseVersion(measure.getReleaseVersion())) {
            String joinedVersions = "(" + StringUtils.join(allowedVersions, ", ") + ")";
            throw new MeasureReleaseVersionInvalidException(measureId, measure.getReleaseVersion(), joinedVersions);
        } else {
            return measure;
        }
    }

    private boolean isValidReleaseVersion(String releaseVersion) {
        if (StringUtils.isEmpty(releaseVersion)) {
            return false;
        } else {
            return allowedVersions.contains(releaseVersion);
        }
    }
}
