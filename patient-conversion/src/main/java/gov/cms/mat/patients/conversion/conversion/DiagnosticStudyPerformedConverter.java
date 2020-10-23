package gov.cms.mat.patients.conversion.conversion;


import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.patients.conversion.conversion.helpers.ObservationConverter;
import gov.cms.mat.patients.conversion.conversion.results.QdmToFhirConversionResult;
import gov.cms.mat.patients.conversion.dao.QdmDataElement;
import gov.cms.mat.patients.conversion.service.CodeSystemEntriesService;
import gov.cms.mat.patients.conversion.service.ValidationService;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DiagnosticStudyPerformedConverter extends ConverterBase<Observation> implements ObservationConverter {
    public static final String QDM_TYPE = "QDM::DiagnosticStudyPerformed";

    public DiagnosticStudyPerformedConverter(CodeSystemEntriesService codeSystemEntriesService,
                                             FhirContext fhirContext,
                                             ObjectMapper objectMapper,
                                             ValidationService validationService) {
        super(codeSystemEntriesService, fhirContext, objectMapper, validationService);
    }

    @Override
    String getQdmType() {
        return QDM_TYPE;
    }

    @Override
    QdmToFhirConversionResult<Observation> convertToFhir(Patient fhirPatient, QdmDataElement qdmDataElement) {
        //http://hl7.org/fhir/us/qicore/qdm-to-qicore.html#8102-diagnostic-study-performed
        return convertToFhirObservation(fhirPatient, qdmDataElement, this);
    }

    @Override
    void convertNegation(QdmDataElement qdmDataElement, Observation observation) {
        convertNegationObservation(qdmDataElement, observation);
    }

}
