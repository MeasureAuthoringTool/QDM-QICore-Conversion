package gov.cms.mat.fhir.services.components.reporting.helpers;

import gov.cms.mat.fhir.rest.dto.MeasureConversionResults;
import org.apache.commons.lang3.StringUtils;

import static gov.cms.mat.fhir.services.components.reporting.HapiResourcePersistedState.EXISTS;

public interface MeasureResultsHelper {
    MeasureConversionResults getMeasureConversionResults();

    default boolean measureExistsInHapi() {
        MeasureConversionResults results = getMeasureConversionResults();

        if (results == null) {
            return false;
        }

        return StringUtils.isNotEmpty(results.getReason()) && results.getReason().equals(EXISTS.value);
    }
}
