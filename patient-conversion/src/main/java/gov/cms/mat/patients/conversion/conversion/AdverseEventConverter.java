package gov.cms.mat.patients.conversion.conversion;

import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.patients.conversion.conversion.results.QdmToFhirConversionResult;
import gov.cms.mat.patients.conversion.dao.QdmCodeSystem;
import gov.cms.mat.patients.conversion.dao.QdmDataElement;
import gov.cms.mat.patients.conversion.service.CodeSystemEntriesService;
import gov.cms.mat.patients.conversion.service.ValidationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.hl7.fhir.r4.model.AdverseEvent;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

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
    String getQdmType() {
        return QDM_TYPE;
    }

    @Override
    QdmToFhirConversionResult convertToFhir(Patient fhirPatient, QdmDataElement qdmDataElement) {
        log.info("patient has  AdverseEvent: {}", fhirPatient.getId());

        AdverseEvent adverseEvent = new AdverseEvent();
        adverseEvent.setId(qdmDataElement.get_id());

        List<QdmCodeSystem> qdmCodeSystems = qdmDataElement.getDataElementCodes();

        if (CollectionUtils.isNotEmpty(qdmCodeSystems)) {
            adverseEvent.setEvent(convertToCodeSystems(codeSystemEntriesService, qdmCodeSystems));
        } else {
            log.warn("Adverse event does not contain any events");
        }

        if (qdmDataElement.getCategory() != null) {
            log.info("We have category"); //todo no data
        }

        if (qdmDataElement.getSeverity() != null) {
            log.info("We have Severity");  //todo no data
        }

        if (qdmDataElement.getAuthorDatetime() != null) {
            log.info("We have AuthorDateTime");  //todo no data
        }

        if (CollectionUtils.isNotEmpty(qdmDataElement.getFacilityLocations())) {
            log.info("We have Locations"); //todo no data
        }

        processNegation(qdmDataElement, adverseEvent);

        return QdmToFhirConversionResult.builder()
                .fhirResource(adverseEvent)
                .conversionMessages(Collections.emptyList())
                .build();
    }
}
