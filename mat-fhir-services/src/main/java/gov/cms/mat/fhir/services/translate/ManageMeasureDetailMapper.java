package gov.cms.mat.fhir.services.translate;

import gov.cms.mat.fhir.commons.model.Measure;
import lombok.extern.slf4j.Slf4j;
import mat.client.measure.ManageCompositeMeasureDetailModel;
import mat.client.measure.ManageMeasureDetailModel;
import mat.server.MeasureLibraryService;
import mat.server.util.MeasureUtility;
import mat.shared.DateUtility;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.UncheckedIOException;

@Slf4j
public class ManageMeasureDetailMapper {
    private final byte[] xmlBytes;
    private final Measure measure;

    public ManageMeasureDetailMapper(byte[] xmlBytes, Measure measure) {
        this.xmlBytes = xmlBytes;
        this.measure = measure;
    }

    public ManageCompositeMeasureDetailModel convert() {
        ManageCompositeMeasureDetailModel model = getFromXml();

        processModel(model);

        if (measure.getIsCompositeMeasure()) {
            createMeasureDetailsModelForCompositeMeasure(model);
        }

        return model;
    }

    private ManageCompositeMeasureDetailModel getFromXml() {
        if (ArrayUtils.isNotEmpty(xmlBytes)) {
            try {
                return MeasureLibraryService.createModelFromXML(new String(xmlBytes));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        } else {
            throw new IllegalArgumentException("Xml bytes are null");
        }
    }

    private void createMeasureDetailsModelForCompositeMeasure(final ManageCompositeMeasureDetailModel model) {
        model.setCompositeScoringMethod(measure.getCompositeScoring());
        model.setQdmVersion(measure.getQdmVersion());
    }


    private void processModel(ManageMeasureDetailModel model) {
        model.setId(measure.getId());
        model.setCalenderYear(model.getPeriodModel().isCalenderYear());

        if (model.getPeriodModel() != null && !model.getPeriodModel().isCalenderYear()) {
            model.setMeasFromPeriod(model.getPeriodModel() != null
                    ? model.getPeriodModel().getStartDate() : null);
            model.setMeasToPeriod(model.getPeriodModel() != null
                    ? model.getPeriodModel().getStopDate() : null);
        }

        model.setEndorseByNQF(StringUtils.isNotBlank(model.getEndorsement()));

        setFormattedVersion(model);

        setOrgVersionNumber(model);

        model.setVersionNumber(MeasureUtility.getVersionText(model.getOrgVersionNumber(), measure.getDraft()));

        model.setFinalizedDate(DateUtility.convertDateToString(measure.getFinalizedDate()));
        model.setDraft(measure.getDraft());
        model.setValueSetDate(DateUtility.convertDateToStringNoTime(measure.getValueSetDate()));
        model.setNqfId(model.getNqfModel() != null
                ? model.getNqfModel().getExtension() : null);


        if (measure.getEmeasureId() != null) {
            model.seteMeasureId(measure.getEmeasureId());
        }


        if (measure.getMeasureOwnerId() != null) {
            model.setMeasureOwnerId(measure.getMeasureOwnerId().getUserId());
        }

        model.setMeasureName(measure.getDescription());
        model.setShortName(measure.getAbbrName());
        model.setMeasScoring(measure.getScoring());

        if (measure.getMeasureSetId() != null) {
            model.setMeasureSetId(measure.getMeasureSetId().getId());
        }


        model.setValueSetDate(DateUtility.convertDateToStringNoTime(measure.getValueSetDate()));
    }

    private void setOrgVersionNumber(ManageMeasureDetailModel model) {
        if (measure.getVersion() == null || measure.getRevisionNumber() == null) {
            log.debug("Cannot set formatted version revision: {}, version: {}",
                    measure.getRevisionNumber(), measure.getVersion());
        } else {
            model.setOrgVersionNumber(MeasureUtility.formatVersionText(measure.getRevisionNumber().toString(),
                    String.valueOf(measure.getVersionNumber())));
        }
    }

    private void setFormattedVersion(ManageMeasureDetailModel model) {
        if (measure.getVersion() == null || measure.getRevisionNumber() == null) {
            log.debug("Cannot set formatted version revision: {}, version: {}",
                    measure.getRevisionNumber(), measure.getVersion());
        } else {
            model.setFormattedVersion(
                    MeasureUtility.getVersionText(measure.getVersion().toString(),
                            measure.getRevisionNumber().toString(),
                            measure.getDraft()));
        }
    }
}
