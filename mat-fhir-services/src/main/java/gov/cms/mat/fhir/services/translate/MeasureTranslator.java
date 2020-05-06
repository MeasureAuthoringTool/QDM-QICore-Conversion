package gov.cms.mat.fhir.services.translate;

import gov.cms.mat.fhir.services.translate.creators.FhirCreator;
import org.hl7.fhir.r4.model.Measure;

public interface MeasureTranslator extends FhirCreator {
    Measure translateToFhir(String uuid);

    default Measure buildMeasure() {
        return new Measure();
    }

    default String createVersion(gov.cms.mat.fhir.commons.model.Measure matMeasure) {
        return createVersion(matMeasure.getVersion(), matMeasure.getRevisionNumber());
    }
}
