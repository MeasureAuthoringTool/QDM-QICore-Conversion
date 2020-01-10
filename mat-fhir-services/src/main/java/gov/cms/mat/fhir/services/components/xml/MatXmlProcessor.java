package gov.cms.mat.fhir.services.components.xml;

import gov.cms.mat.fhir.commons.model.MatXmlBytes;
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
    private final MeasureExportRepository measureExportRepo;
    private final MeasureRepository measureRepository;
    private MeasureXmlRepository measureXmlRepository;

    public MatXmlProcessor(MeasureXmlRepository measureXmlRepository,
                           MeasureExportRepository measureExportRepo,
                           MeasureRepository measureRepository) {
        this.measureXmlRepository = measureXmlRepository;
        this.measureExportRepo = measureExportRepo;
        this.measureRepository = measureRepository;
    }

    public byte[] getXmlById(String measureId, XmlSource xmlSource) {
        Optional<Measure> optionalMeasure = measureRepository.findById(measureId);

        return optionalMeasure
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
                throw new IllegalArgumentException("No source for " + xmlSource); // should never-ever get here
        }
    }

    byte[] getSimpleXml(Measure measure) {
        Optional<MeasureExport> optionalMeasureExport = measureExportRepo.findByMeasureId(measure.getId());
        return processBytes(optionalMeasureExport);
    }

    byte[] getMeasureXml(Measure measure) {
        Optional<MeasureXml> optionalMeasureXml = measureXmlRepository.findByMeasureId(measure);
        return processBytes(optionalMeasureXml);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private byte[] processBytes(Optional<? extends MatXmlBytes> optionalMatXmlBytes) {
        return optionalMatXmlBytes
                .map(MatXmlBytes::getXmlBytes)
                .orElse(null);
    }
}
