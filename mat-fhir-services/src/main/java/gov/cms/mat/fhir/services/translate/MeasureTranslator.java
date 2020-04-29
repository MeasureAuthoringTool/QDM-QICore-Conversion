package gov.cms.mat.fhir.services.translate;

import gov.cms.mat.fhir.services.translate.creators.FhirCreator;
import org.hl7.fhir.r4.model.Measure;

public interface MeasureTranslator extends FhirCreator {
    Measure translateToFhir(String uuid);

    default Measure buildMeasure() {
        Measure fhirMeasure = new Measure();

        return fhirMeasure;
    }

}
