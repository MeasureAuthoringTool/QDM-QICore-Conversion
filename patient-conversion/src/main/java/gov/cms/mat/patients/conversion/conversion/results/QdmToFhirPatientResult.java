package gov.cms.mat.patients.conversion.conversion.results;

import gov.cms.mat.patients.conversion.data.ConversionOutcome;
import lombok.Builder;
import lombok.Data;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Patient;

import java.util.List;

@Builder
@Data
public class QdmToFhirPatientResult {
    private final Patient fhirPatient;
    private final ConversionOutcome outcome;
}
