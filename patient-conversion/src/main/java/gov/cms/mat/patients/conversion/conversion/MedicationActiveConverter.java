package gov.cms.mat.patients.conversion.conversion;


import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.patients.conversion.conversion.results.QdmToFhirConversionResult;
import gov.cms.mat.patients.conversion.dao.QdmDataElement;
import gov.cms.mat.patients.conversion.exceptions.InvalidUnitException;
import gov.cms.mat.patients.conversion.service.CodeSystemEntriesService;
import gov.cms.mat.patients.conversion.service.ValidationService;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Dosage;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Timing;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class MedicationActiveConverter extends ConverterBase<MedicationRequest> {
    public static final String QDM_TYPE = "QDM::MedicationActive";

    public MedicationActiveConverter(CodeSystemEntriesService codeSystemEntriesService,
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
    public QdmToFhirConversionResult<MedicationRequest> convertToFhir(Patient fhirPatient, QdmDataElement qdmDataElement) {
        List<String> conversionMessages = new ArrayList<>();

        MedicationRequest medicationRequest = new MedicationRequest();
        medicationRequest.setId(qdmDataElement.get_id());
        medicationRequest.setSubject(createReference(fhirPatient));

        //http://hl7.org/fhir/us/qicore/qdm-to-qicore.html#8171-medication-active
        medicationRequest.setStatus(MedicationRequest.MedicationRequestStatus.ACTIVE); //Constrain to “active”
        medicationRequest.setIntent(MedicationRequest.MedicationRequestIntent.ORDER); //Constrain to “order”

        medicationRequest.setMedication(convertToCodeSystems(codeSystemEntriesService, qdmDataElement.getDataElementCodes()));

        if (qdmDataElement.getDosage() != null) {
            try {
                Quantity quantity = convertQuantity(qdmDataElement.getDosage());

                Dosage dosage = medicationRequest.getDosageInstructionFirstRep();
                dosage.getDoseAndRateFirstRep().setDose(quantity);
            } catch (InvalidUnitException e) {
                conversionMessages.add(e.getMessage());
            }
        }

        if (qdmDataElement.getFrequency() != null) {
            if (qdmDataElement.getFrequency().getCode() == null) {
                conversionMessages.add("Frequency code is null");
            } else {
                Dosage dosage = medicationRequest.getDosageInstructionFirstRep();
                Timing timing = dosage.getTiming();
                timing.setCode(convertToCodeableConcept(codeSystemEntriesService, qdmDataElement.getFrequency()));
            }
        }

        if (qdmDataElement.getRoute() != null) {
            Dosage dosage = medicationRequest.getDosageInstructionFirstRep();
            dosage.setRoute(convertToCodeableConcept(codeSystemEntriesService, qdmDataElement.getRoute()));
        }


        if (qdmDataElement.getDosage() != null) {
            Dosage dosage = medicationRequest.getDosageInstructionFirstRep();
            Dosage.DosageDoseAndRateComponent dosageDoseAndRateComponent = dosage.getDoseAndRateFirstRep();
            dosageDoseAndRateComponent.setDose(convertQuantity(qdmDataElement.getDosage()));
        }

        if (qdmDataElement.getSetting() != null) {
            log.warn("Not mapping -> qdmDataElement.getSetting()");
        }

        if (qdmDataElement.getPrescriber() != null) {
            log.warn("Not mapping -> qdmDataElement.getPrescriber()");
        }

        if (qdmDataElement.getDispenser() != null) {
            log.warn("Not mapping -> qdmDataElement.getDispenser() ");
        }

        if( qdmDataElement.getSupply() != null) {
            log.warn("Not mapping -> qdmDataElement.getSupply()");
        }

        if (qdmDataElement.getRelevantPeriod() != null) {
            Dosage dosage = medicationRequest.getDosageInstructionFirstRep();
            Timing timing = dosage.getTiming();
            Timing.TimingRepeatComponent timingRepeatComponent = timing.getRepeat();
            timingRepeatComponent.setBounds(createFhirPeriod(qdmDataElement.getRelevantPeriod()));
        }

        return QdmToFhirConversionResult.<MedicationRequest>builder()
                .fhirResource(medicationRequest)
                .conversionMessages(conversionMessages)
                .build();
    }
}

