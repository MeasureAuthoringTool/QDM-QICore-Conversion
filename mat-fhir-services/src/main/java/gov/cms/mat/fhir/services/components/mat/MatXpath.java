package gov.cms.mat.fhir.services.components.mat;

import mat.server.util.XmlProcessor;
import org.springframework.stereotype.Component;

@Component
public class MatXpath {
    static final String MEASURE_DETAILS_TAG = "measureDetails";
    static final String CQL_LOOK_UP_TAG = "cqlLookUp";
    static final String SUPPLEMENTAL_DATA_TAG = "supplementalDataElements";
    static final String RISK_ADJUSTMENTS_TAG = "riskAdjustmentVariables";
    static final String MEASURE_GROUPING_TAG = "measureGrouping";

    String toCompositeMeasureDetail(String xml) {
        return processXmlTag(xml, MEASURE_DETAILS_TAG);
    }

    String toQualityData(String xml) {
        return processXmlTag(xml, CQL_LOOK_UP_TAG);
    }

    String toCQLDefinitionsSupplementalData(String xml) {
        return processXmlTag(xml, SUPPLEMENTAL_DATA_TAG);
    }

    String toCQLDefinitionsRiskAdjustments(String xml) {
        return processXmlTag(xml, RISK_ADJUSTMENTS_TAG);
    }

    String toMeasureGrouping(String xml) {
        return processXmlTag(xml, MEASURE_GROUPING_TAG);
    }

    String processXmlTag(String xml, String tag) {
        return new XmlProcessor(xml).getXmlByTagName(tag);
    }


    String processXmlValue(String xml, String tag) {
        return new XmlProcessor(xml).getValueByTagName(tag);
    }
}
