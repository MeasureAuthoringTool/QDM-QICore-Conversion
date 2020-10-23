package gov.cms.mat.patients.conversion.conversion;


import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.patients.conversion.conversion.helpers.ObservationConverter;
import gov.cms.mat.patients.conversion.conversion.helpers.ServiceRequestConverter;
import gov.cms.mat.patients.conversion.conversion.results.QdmToFhirConversionResult;
import gov.cms.mat.patients.conversion.dao.QdmDataElement;
import gov.cms.mat.patients.conversion.service.CodeSystemEntriesService;
import gov.cms.mat.patients.conversion.service.ValidationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LaboratoryTestPerformedConverter extends ConverterBase<Observation> implements ObservationConverter {
    public static final String QDM_TYPE = "QDM::LaboratoryTestPerformed";

    public LaboratoryTestPerformedConverter(CodeSystemEntriesService codeSystemEntriesService,
                                            FhirContext fhirContext,
                                            ObjectMapper objectMapper,
                                            ValidationService validationService) {
        super(codeSystemEntriesService, fhirContext, objectMapper, validationService);
    }

    @Override
    public String getQdmType() {
        return QDM_TYPE;
    }

    @Override
    public QdmToFhirConversionResult<Observation> convertToFhir(Patient fhirPatient, QdmDataElement qdmDataElement) {
        // http://hl7.org/fhir/us/qicore/qdm-to-qicore.html#8162-laboratory-test-performed
        //	Constrain status to -  final, amended, corrected
       var result = convertToFhirObservation(fhirPatient,
                qdmDataElement,
                this);


       return result;
    }
}
