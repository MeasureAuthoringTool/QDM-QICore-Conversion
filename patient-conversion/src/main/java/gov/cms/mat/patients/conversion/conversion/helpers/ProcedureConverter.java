package gov.cms.mat.patients.conversion.conversion.helpers;

import gov.cms.mat.patients.conversion.conversion.ConverterBase;
import gov.cms.mat.patients.conversion.conversion.results.QdmToFhirConversionResult;
import gov.cms.mat.patients.conversion.dao.QdmDataElement;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Procedure;

import java.util.ArrayList;
import java.util.List;

public interface ProcedureConverter extends DataElementFinder, FhirCreator {

    default QdmToFhirConversionResult<Procedure> convertToFhirProcedure(Patient fhirPatient,
                                                                        QdmDataElement qdmDataElement,
                                                                        ConverterBase<Procedure> converterBase) {
        List<String> conversionMessages = new ArrayList<>();
        Procedure procedure = new Procedure();
        procedure.setId(qdmDataElement.get_id());

        procedure.setSubject(createReference(fhirPatient));


        procedure.setPerformed(createFhirPeriod(qdmDataElement.getRelevantPeriod()));
        /**
         * {
         * "dataTypeDescription": "Intervention, Performed",
         * "matAttributeName": "authorDatetime",
         * "fhirQicoreMapping": "ServiceRequest.authoredOn",
         * "fhirResource": "Procedure",
         * "fhirType": "dateTime",
         * "fhirElement": "authoredOn",
         * "helpWording": "Definition: When the request transitioned to being actionable.\n",
         * "dropDown": []
         * },
         */
        //todo stan
        // http://hl7.org/fhir/us/qicore/qdm-to-qicore.html#8152-intervention-performed
        // procedure.setAuthoredOn todo how to map this see negataion


        if (qdmDataElement.getReason() != null) {
            procedure.setReasonCode(List.of(convertToCodeableConcept(converterBase.getCodeSystemEntriesService(), qdmDataElement.getReason())));
        }

        CodeableConcept codeableConcept = convertToCodeSystems(converterBase.getCodeSystemEntriesService(), qdmDataElement.getDataElementCodes());
        procedure.setCode(codeableConcept);

        if (!converterBase.processNegation(qdmDataElement, procedure)) {
            // http://hl7.org/fhir/us/qicore/qdm-to-qicore.html#8152-intervention-performed
            // constrain to “completed”
            procedure.setStatus(Procedure.ProcedureStatus.COMPLETED);
        }

        return QdmToFhirConversionResult.<Procedure>builder()
                .fhirResource(procedure)
                .conversionMessages(conversionMessages)
                .build();
    }
}
