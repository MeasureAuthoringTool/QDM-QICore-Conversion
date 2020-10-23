package gov.cms.mat.patients.conversion.conversion;

import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.patients.conversion.conversion.results.QdmToFhirConversionResult;
import gov.cms.mat.patients.conversion.dao.QdmDataElement;
import gov.cms.mat.patients.conversion.service.CodeSystemEntriesService;
import gov.cms.mat.patients.conversion.service.ValidationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.hl7.fhir.r4.model.AdverseEvent;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@Slf4j
public class AdverseEventConverter extends ConverterBase<AdverseEvent> {
    public static final String QDM_TYPE = "QDM::AdverseEvent";

    public AdverseEventConverter(CodeSystemEntriesService codeSystemEntriesService,
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
    public QdmToFhirConversionResult<AdverseEvent> convertToFhir(Patient fhirPatient, QdmDataElement qdmDataElement) {
        log.info("patient has  AdverseEvent: {}", fhirPatient.getId());

        AdverseEvent adverseEvent = new AdverseEvent();
        adverseEvent.setId(qdmDataElement.get_id());

        if (CollectionUtils.isNotEmpty(qdmDataElement.getDataElementCodes())) {
            adverseEvent.setEvent(convertToCodeSystems(codeSystemEntriesService, qdmDataElement.getDataElementCodes()));
        }

//        if (qdmDataElement.getCategory() != null) {
//            log.info("We have category"); //todo no data
//        }
//
//        if (qdmDataElement.getSeverity() != null) {
//            log.info("We have Severity");  //todo no data
//        }
//
//        if (qdmDataElement.getAuthorDatetime() != null) {
//            log.info("We have AuthorDateTime");  //todo no data
//        }
//
//        if (CollectionUtils.isNotEmpty(qdmDataElement.getFacilityLocations())) {
//            log.info("We have Locations"); //todo no data
//        }

        processNegation(qdmDataElement, adverseEvent);

        return QdmToFhirConversionResult.<AdverseEvent>builder()
                .fhirResource(adverseEvent)
                .conversionMessages(Collections.emptyList())
                .build();
    }
}
