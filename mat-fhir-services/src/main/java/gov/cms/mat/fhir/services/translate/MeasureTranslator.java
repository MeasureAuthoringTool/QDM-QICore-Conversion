package gov.cms.mat.fhir.services.translate;

import gov.cms.mat.fhir.services.translate.creators.FhirCreator;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.Period;
import java.time.LocalDate;
import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;

public interface MeasureTranslator extends FhirCreator {
    Measure translateToFhir(String uuid);

    default Measure buildMeasure() {
        return new Measure();
    }

    default String createVersion(gov.cms.mat.fhir.commons.model.Measure matMeasure) {
        return createVersion(matMeasure.getVersion(), matMeasure.getRevisionNumber());
    }

    default Period buildDefaultPeriod() {
        LocalDate now = LocalDate.now();

        return new Period()
                .setStart(java.sql.Date.valueOf(now.with(firstDayOfYear())))
                .setEnd(java.sql.Date.valueOf(now.with(lastDayOfYear())));
    }
}
