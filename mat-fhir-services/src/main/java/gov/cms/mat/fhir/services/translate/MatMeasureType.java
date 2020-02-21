package gov.cms.mat.fhir.services.translate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Slf4j
public enum MatMeasureType {
    /* Commented is the mat representation*/
    COMPOSITE("composite"),
    INTERM_OM(FhirCodes.OUTCOME), /* "INTERM-OM" */ OUTCOME(FhirCodes.OUTCOME),
    PRO_PM("patient-report-outcome"), /* "PRO-PM" */
    STRUCTURE(FhirCodes.STRUCTURE), RESOURCE(FhirCodes.STRUCTURE),
    APPROPRIATE(FhirCodes.PROCESS), EFFICIENCY(FhirCodes.PROCESS), PROCESS(FhirCodes.PROCESS);

    public final String fhirCode;

    MatMeasureType(String fhirCode) {
        this.fhirCode = fhirCode;
    }

    public static Optional<MatMeasureType> findByMatAbbreviation(String abbrName) {
        if (StringUtils.isEmpty(abbrName)) {
            return Optional.empty();
        } else {
            return find(abbrName);
        }
    }

    private static Optional<MatMeasureType> find(String abbrName) {
        String enumName = abbrName.replace('-', '_');

        try {
            return Optional.of(MatMeasureType.valueOf(enumName));
        } catch (Exception e) {
            log.info("Cannot find MatMeasureType abbrName: {}, enumName: {}", abbrName, enumName);
            return Optional.empty();
        }
    }


    static class FhirCodes {
        static final String PROCESS = "process";
        static final String OUTCOME = "outcome";
        static final String STRUCTURE = "structure";

        private FhirCodes() {
        }
    }
}
