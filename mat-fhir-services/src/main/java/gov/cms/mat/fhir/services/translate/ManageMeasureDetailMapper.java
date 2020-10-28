package gov.cms.mat.fhir.services.translate;

import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.services.components.mat.MatXmlConverter;
import gov.cms.mat.fhir.services.components.mat.MatXmlException;
import lombok.extern.slf4j.Slf4j;
import mat.client.measure.ManageCompositeMeasureDetailModel;
import mat.client.measure.ManageMeasureDetailModel;
import mat.server.util.MeasureUtility;
import mat.shared.DateUtility;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ManageMeasureDetailMapper {
    private final MatXmlConverter matXmlConverter;

    public ManageMeasureDetailMapper(MatXmlConverter matXmlConverter) {
        this.matXmlConverter = matXmlConverter;
    }

    public ManageCompositeMeasureDetailModel convert(byte[] xmlBytes, Measure measure) {
        ManageCompositeMeasureDetailModel model = getFromXml(xmlBytes);

        if (model == null) {
            log.trace("Cannot Convert to object xml: {}", new String(xmlBytes)); // never turn on in production
            throw new MatXmlException("Cannot process xml bytes");
        } else {
            return processMeasureAndModel(measure, model);
        }
    }

    private ManageCompositeMeasureDetailModel processMeasureAndModel(Measure measure,
                                                                     ManageCompositeMeasureDetailModel model) {
        processModel(model, measure);

        if (measure.getIsCompositeMeasure()) {
            processCompositeMeasure(model, measure);
        }

        return model;
    }

    private ManageCompositeMeasureDetailModel getFromXml(byte[] xmlBytes) {
        if (ArrayUtils.isNotEmpty(xmlBytes)) {
            return matXmlConverter.toCompositeMeasureDetail(new String(xmlBytes));
        } else {
            throw new IllegalArgumentException("Xml bytes are null");
        }
    }

    private void processCompositeMeasure(final ManageCompositeMeasureDetailModel model,
                                         Measure measure) {
        model.setCompositeScoringMethod(measure.getCompositeScoring());
        model.setQdmVersion(measure.getQdmVersion());
    }


    private void processModel(ManageMeasureDetailModel model, Measure measure) {
        model.setId(measure.getId());

        if (model.getPeriodModel() != null) {
            processPeriod(model);
        }

        model.setEndorseByNQF(StringUtils.isNotBlank(model.getEndorsement()));

        setFormattedVersion(model, measure);

        setOrgVersionNumber(model, measure);

        if (model.getOrgVersionNumber() != null) {
            model.setVersionNumber(MeasureUtility.getVersionText(model.getOrgVersionNumber(), measure.getDraft()));
        }

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
        model.setPopulationBasis(measure.getPopulationBasis());

        if (measure.getMeasureSetId() != null) {
            model.setMeasureSetId(measure.getMeasureSetId().getId());
        }

        model.setValueSetDate(DateUtility.convertDateToStringNoTime(measure.getValueSetDate()));
    }

    private void processPeriod(ManageMeasureDetailModel model) {
        model.setCalenderYear(model.getPeriodModel().isCalenderYear());

        if (model.getPeriodModel() != null && !model.getPeriodModel().isCalenderYear()) {
            model.setMeasFromPeriod(model.getPeriodModel() != null
                    ? model.getPeriodModel().getStartDate() : null);
            model.setMeasToPeriod(model.getPeriodModel() != null
                    ? model.getPeriodModel().getStopDate() : null);

        }
    }

    private void setOrgVersionNumber(ManageMeasureDetailModel model, Measure measure) {
        if (measure.getVersion() == null || measure.getRevisionNumber() == null) {
            log.debug("Cannot set formatted version revision: {}, version: {}",
                    measure.getRevisionNumber(), measure.getVersion());
        } else {
            model.setOrgVersionNumber(MeasureUtility.formatVersionText(measure.getRevisionNumber().toString(),
                    String.valueOf(measure.getVersionNumber())));
        }
    }

    private void setFormattedVersion(ManageMeasureDetailModel model, Measure measure) {
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
