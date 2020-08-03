package gov.cms.mat.fhir.services.translate;

import gov.cms.mat.fhir.services.components.reporting.ConversionReporter;
import lombok.extern.slf4j.Slf4j;
import mat.client.measure.ManageCompositeMeasureDetailModel;
import mat.model.MeasureType;
import org.apache.commons.collections4.CollectionUtils;
import org.hl7.fhir.r4.model.Measure;

import java.util.Optional;

@Slf4j
public class MeasureResultProcessor {
    private static final String UNKNOWN = "MAT.Unknown";

    private final ManageCompositeMeasureDetailModel matCompositeMeasureModel;
    private final String humanReadable;
    private final Measure fhirMeasure;

    public MeasureResultProcessor(ManageCompositeMeasureDetailModel matCompositeMeasureModel,
                                  String humanReadable,
                                  Measure fhirMeasure) {
        this.matCompositeMeasureModel = matCompositeMeasureModel;
        this.humanReadable = humanReadable;
        this.fhirMeasure = fhirMeasure;
    }

    void generateMeasureResults() {
        if (humanReadable.isEmpty()) {
            ConversionReporter.setMeasureResult("MAT.humanReadible", "Measure.text", "Is Empty");
        }

        ConversionReporter.setMeasureResult(UNKNOWN, "Measure.meta.extension", "No mapping available");
        ConversionReporter.setMeasureResult("MAT.Id", "Measure.url", "Generated From MAT Measure id (UUID)");
        ConversionReporter.setMeasureResult(UNKNOWN, "Measure.contact", "No Mapping default to cms.gov");
        ConversionReporter.setMeasureResult(UNKNOWN, "Measure.jurisdiction", "No Mapping defaulting to US");
        ConversionReporter.setMeasureResult(UNKNOWN, "Measure.purpose", "No Mapping defaulting to Unknown");
        ConversionReporter.setMeasureResult(UNKNOWN, "Measure.topic", "No Mapping default Health Quality Measure Document");


        //set measure status mat qdm does not have all status types
        if (matCompositeMeasureModel.isDraft() || matCompositeMeasureModel.isDeleted()) {
            log.info("Status is OKAY");
        } else {
            ConversionReporter.setMeasureResult(UNKNOWN, "Measure.status", "Defaulting to ACTIVE neither draft or deleted");
        }

        if (matCompositeMeasureModel.getFinalizedDate() == null) {
            ConversionReporter.setMeasureResult("MAT.finalizedDate", "Measure.approvalDate", "Finalized Date is NULL");
        }

        if (CollectionUtils.isEmpty(fhirMeasure.getIdentifier())) {
            ConversionReporter.setMeasureResult("MAT.eMeasureId", "Measure.identifier", "Not Available");
            ConversionReporter.setMeasureResult("MAT.nqfId", "Measure.identifier", "Not Available");
        }

        if (CollectionUtils.isEmpty(fhirMeasure.getRelatedArtifact())) {
            ConversionReporter.setMeasureResult("MAT.referencesList", "Measure.relatedArtifact", "NO Citations");
        }

        if (CollectionUtils.isEmpty(matCompositeMeasureModel.getMeasureTypeSelectedList())) {
            ConversionReporter.setMeasureResult("MAT.measureType", "Measure.type", "No Measure Types Found");
        } else {
            matCompositeMeasureModel.getMeasureTypeSelectedList()
                    .forEach(this::checkType);
        }

    }

    private void checkType(MeasureType m) {
        Optional<MatMeasureType> optional = MatMeasureType.findByMatAbbreviation(m.getAbbrName());

        if (!optional.isPresent()) {
            ConversionReporter.setMeasureResult("MAT.measureType",
                    "Measure.type",
                    "No Measure Type Found for " + m.getAbbrName());
        }
    }

}
