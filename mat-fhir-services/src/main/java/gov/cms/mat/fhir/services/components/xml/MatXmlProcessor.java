package gov.cms.mat.fhir.services.components.xml;

import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.commons.model.MeasureExport;
import gov.cms.mat.fhir.commons.model.MeasureXml;
import gov.cms.mat.fhir.services.repository.MeasureExportRepository;
import gov.cms.mat.fhir.services.repository.MeasureRepository;
import gov.cms.mat.fhir.services.repository.MeasureXmlRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MatXmlProcessor {
    private final MeasureXmlRepository measureXmlRepository;
    private final MeasureExportRepository measureExportRepo;
    private final MeasureRepository measureRepository;

    public MatXmlProcessor(MeasureXmlRepository measureXmlRepository,
                           MeasureExportRepository measureExportRepo,
                           MeasureRepository measureRepository) {
        this.measureXmlRepository = measureXmlRepository;
        this.measureExportRepo = measureExportRepo;
        this.measureRepository = measureRepository;
    }

    public byte[] getXmlById(String measureId, XmlSource xmlSource) {
        Optional<Measure> optional = measureRepository.findById(measureId);

        return optional
                .map(measure -> getXml(measure, xmlSource))
                .orElse(null);
    }


    public byte[] getXml(Measure measure, XmlSource xmlSource) {
        switch (xmlSource) {
            case SIMPLE:
                return getSimpleXml(measure);
            case MEASURE:
                return getMeasureXml(measure);
            default:
                throw new IllegalArgumentException("No source for " + xmlSource);
        }
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
