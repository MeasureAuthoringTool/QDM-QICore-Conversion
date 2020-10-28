package gov.cms.mat.patients.conversion.conversion.helpers;

import gov.cms.mat.patients.conversion.conversion.ConverterBase;
import gov.cms.mat.patients.conversion.conversion.results.QdmToFhirConversionResult;
import gov.cms.mat.patients.conversion.dao.QdmCodeSystem;
import gov.cms.mat.patients.conversion.dao.QdmDataElement;
import gov.cms.mat.patients.conversion.exceptions.InvalidUnitException;
import gov.cms.mat.patients.conversion.exceptions.PatientConversionException;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Dosage;
import org.hl7.fhir.r4.model.Duration;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Timing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static gov.cms.mat.patients.conversion.conversion.ConverterBase.NO_STATUS_MAPPING;

public interface MedicationRequestConverter extends FhirCreator, DataElementFinder {

    default QdmToFhirConversionResult<MedicationRequest> convertToFhirMedicationRequest(Patient fhirPatient,
                                                                                        QdmDataElement qdmDataElement,
                                                                                        ConverterBase<MedicationRequest> converterBase,
                                                                                        MedicationRequest.MedicationRequestIntent intent) {
        List<String> conversionMessages = new ArrayList<>();

        MedicationRequest medicationRequest = new MedicationRequest();
        medicationRequest.setId(qdmDataElement.get_id());
        medicationRequest.setSubject(createReference(fhirPatient));
        medicationRequest.setMedication(getMedicationCodeableConcept(qdmDataElement.getDataElementCodes(), converterBase));
        medicationRequest.setAuthoredOn(qdmDataElement.getAuthorDatetime());

        medicationRequest.setIntent(intent);

        if (qdmDataElement.getDosage() != null) {
            try {
                Quantity quantity = convertQuantity(qdmDataElement.getDosage());

                Dosage dosage = medicationRequest.getDosageInstructionFirstRep();
                dosage.getDoseAndRateFirstRep().setDose(quantity);
            } catch (InvalidUnitException e) {
                conversionMessages.add(e.getMessage());
            }
        }

        if (qdmDataElement.getRoute() != null) {
            Dosage dosage = medicationRequest.getDosageInstructionFirstRep();
            dosage.setRoute(convertToCodeableConcept(converterBase.getCodeSystemEntriesService(), qdmDataElement.getRoute()));
        }

        if (qdmDataElement.getRelevantPeriod() != null) {
            Dosage dosage = medicationRequest.getDosageInstructionFirstRep();
            Timing timing = dosage.getTiming();
            Timing.TimingRepeatComponent timingRepeatComponent = timing.getRepeat();
            timingRepeatComponent.setBounds(createFhirPeriod(qdmDataElement.getRelevantPeriod()));
        }

        if (qdmDataElement.getSupply() != null) {
            try {
                Quantity quantity = convertQuantity(qdmDataElement.getSupply());
                medicationRequest.getDispenseRequest().setQuantity(quantity);
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
                timing.setCode(convertToCodeableConcept(converterBase.getCodeSystemEntriesService(), qdmDataElement.getFrequency()));
            }
        }

        if (!converterBase.processNegation(qdmDataElement, medicationRequest)) {
            // http://hl7.org/fhir/us/qicore/qdm-to-qicore.html#8173-medication-discharge
            // 	Constrain to active, completed, on-hold
            medicationRequest.setStatus(MedicationRequest.MedicationRequestStatus.UNKNOWN);
            conversionMessages.add(NO_STATUS_MAPPING);
        }

        if (qdmDataElement.getDaysSupplied() != null) {
            MedicationRequest.MedicationRequestDispenseRequestComponent dispenseRequest = medicationRequest.getDispenseRequest();
            Duration duration = new Duration();
            duration.setUnit("d");
            duration.setSystem("http://unitsofmeasure.org");
            duration.setValue(qdmDataElement.getDaysSupplied());
            dispenseRequest.setExpectedSupplyDuration(duration);
        }

        if (qdmDataElement.getRefills() != null) {
            MedicationRequest.MedicationRequestDispenseRequestComponent dispenseRequest = medicationRequest.getDispenseRequest();
            dispenseRequest.setNumberOfRepeatsAllowed(qdmDataElement.getRefills());
        }


        if (qdmDataElement.getSetting() != null) {
            throw new PatientConversionException("Not mapping -> qdmDataElement.getSetting()");
        }

        if (qdmDataElement.getPrescriber() != null) {
            throw new PatientConversionException("Not mapping -> qdmDataElement.getPrescriber()");
        }

        if (qdmDataElement.getDispenser() != null) {
            throw new PatientConversionException("Not mapping -> qdmDataElement.getDispenser() ");
        }

        if( qdmDataElement.getSupply() != null) {
            throw new PatientConversionException("Not mapping -> qdmDataElement.getSupply()");
        }

        return QdmToFhirConversionResult.<MedicationRequest>builder()
                .fhirResource(medicationRequest)
                .conversionMessages(conversionMessages)
                .build();
    }

    private CodeableConcept getMedicationCodeableConcept(List<QdmCodeSystem> dataElementCodes, ConverterBase<MedicationRequest> converterBase) {
        CodeableConcept codeableConcept = new CodeableConcept();
        codeableConcept.addCoding(createCodingFromDataElementCodes(converterBase.getCodeSystemEntriesService(), dataElementCodes));

        return codeableConcept;
    }
}
