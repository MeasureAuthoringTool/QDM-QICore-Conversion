package gov.cms.mat.patients.conversion.conversion.results;

import lombok.Builder;
import lombok.Data;
import org.hl7.fhir.instance.model.api.IBaseResource;

import java.util.List;

@Builder
@Data
public class QdmToFhirConversionResult {
    List<String> conversionMessages;
    IBaseResource fhirResource;
}
