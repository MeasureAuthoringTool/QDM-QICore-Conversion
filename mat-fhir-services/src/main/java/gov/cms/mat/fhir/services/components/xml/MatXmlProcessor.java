package gov.cms.mat.fhir.services.components.xml;

import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.commons.model.MeasureExport;
import gov.cms.mat.fhir.commons.model.MeasureXml;
import gov.cms.mat.fhir.services.repository.MeasureExportRepository;
import gov.cms.mat.fhir.services.repository.MeasureXmlRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MatXmlProcessor {
    private final MeasureXmlRepository measureXmlRepository;
    private final MeasureExportRepository measureExportRepo;

    public MatXmlProcessor(MeasureXmlRepository measureXmlRepository,
                           MeasureExportRepository measureExportRepo) {
        this.measureXmlRepository = measureXmlRepository;
        this.measureExportRepo = measureExportRepo;
    }

    public byte[] getSimpleXml(Measure measure) {
        Optional<MeasureExport> optional = measureExportRepo.findByMeasureId(measure);

        return optional
                .map(MeasureExport::getSimpleXml)
                .orElse(null);
    }

    public byte[] getMeasureXml(Measure measure) {
        Optional<MeasureXml> optional = measureXmlRepository.findByMeasureId(measure);

        return optional
                .map(MeasureXml::getMeasureXml)
                .orElse(null);
    }
}
