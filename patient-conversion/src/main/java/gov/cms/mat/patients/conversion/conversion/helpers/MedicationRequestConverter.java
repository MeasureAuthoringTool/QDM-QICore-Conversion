package gov.cms.mat.patients.conversion.conversion.helpers;

import gov.cms.mat.patients.conversion.conversion.ConverterBase;
import gov.cms.mat.patients.conversion.conversion.results.QdmToFhirConversionResult;
import gov.cms.mat.patients.conversion.dao.QdmCodeSystem;
import gov.cms.mat.patients.conversion.dao.QdmDataElement;
import gov.cms.mat.patients.conversion.exceptions.PatientConversionException;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Quantity;
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

        Logger log = LoggerFactory.getLogger(MedicationRequestConverter.class);
        List<String> conversionMessages = new ArrayList<>();

        MedicationRequest medicationRequest = new MedicationRequest();
        medicationRequest.setId(qdmDataElement.get_id());
        medicationRequest.setSubject(createReference(fhirPatient));
        medicationRequest.setMedication(getMedicationCodeableConcept(qdmDataElement.getDataElementCodes(), converterBase));
        medicationRequest.setAuthoredOn(qdmDataElement.getAuthorDatetime());
        //Todo for MedicationDischarge there is no intent ?
        medicationRequest.setIntent(intent);

        if (qdmDataElement.getRoute() != null) {
            //  medicationRequest.setDosageInstruction()
            // todo Still NO data
            log.info("We have a dosage");
        }

        if (qdmDataElement.getSupply() != null) {
            MedicationRequest.MedicationRequestDispenseRequestComponent dispenseRequest = medicationRequest.getDispenseRequest();

            Quantity quantity = new Quantity();
            quantity.setValue(qdmDataElement.getSupply().getValue());
            quantity.setSystem("http://unitsofmeasure.org");

            try {
                quantity.setCode(convertUnitToCode(qdmDataElement.getSupply().getUnit()));
            } catch (PatientConversionException e) {
                conversionMessages.add(e.getMessage());
            }

            dispenseRequest.setQuantity(quantity);
        }

        if (!converterBase.processNegation(qdmDataElement, medicationRequest)) {
            // http://hl7.org/fhir/us/qicore/qdm-to-qicore.html#8173-medication-discharge
            // 	Constrain to active, completed, on-hold
            medicationRequest.setStatus(MedicationRequest.MedicationRequestStatus.UNKNOWN);
            conversionMessages.add(NO_STATUS_MAPPING);
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
