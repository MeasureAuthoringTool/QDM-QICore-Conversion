package gov.cms.mat.patients.conversion.conversion;


import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.patients.conversion.conversion.helpers.ServiceRequestConverter;
import gov.cms.mat.patients.conversion.conversion.results.QdmToFhirConversionResult;
import gov.cms.mat.patients.conversion.dao.QdmDataElement;
import gov.cms.mat.patients.conversion.service.CodeSystemEntriesService;
import gov.cms.mat.patients.conversion.service.ValidationService;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EncounterOrderConverter extends ConverterBase<ServiceRequest> implements ServiceRequestConverter {
    public static final String QDM_TYPE = "QDM::EncounterOrder";

    public EncounterOrderConverter(CodeSystemEntriesService codeSystemEntriesService,
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
    public QdmToFhirConversionResult<ServiceRequest> convertToFhir(Patient fhirPatient, QdmDataElement qdmDataElement) {
        //http://hl7.org/fhir/us/qicore/qdm-to-qicore.html#8113-encounter-order
        //Constrain only to “order” (include children: original-order, reflex-order, filler-order, instance-order)
        QdmToFhirConversionResult<ServiceRequest> result = convertToFhirServiceRequest(fhirPatient,
                qdmDataElement,
                this,
                ServiceRequest.ServiceRequestIntent.ORDER);


        if (qdmDataElement.getFacilityLocations() != null) {
            log.debug("HI"); //todo can it exists
        }

        if (qdmDataElement.getPriority() != null) {
            log.debug("HI"); //todo can it exists
        }

        return result;
    }

    @Override
    void convertNegation(QdmDataElement qdmDataElement, ServiceRequest serviceRequest) {
        convertNegationServiceRequest(qdmDataElement, serviceRequest);
    }
}
