package gov.cms.mat.patients.conversion.conversion.helpers;

import gov.cms.mat.patients.conversion.conversion.ConverterBase;
import gov.cms.mat.patients.conversion.conversion.results.QdmToFhirConversionResult;
import gov.cms.mat.patients.conversion.dao.QdmDataElement;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;

import java.util.ArrayList;
import java.util.List;

import static gov.cms.mat.patients.conversion.conversion.ConverterBase.NO_STATUS_MAPPING;

public interface ObservationConverter extends FhirCreator, DataElementFinder {

    default QdmToFhirConversionResult<Observation> convertToFhirObservation(Patient fhirPatient,
                                                                            QdmDataElement qdmDataElement,
                                                                            ConverterBase<Observation> converterBase) {

        Observation observation = new Observation();
        List<String> conversionMessages = new ArrayList<>();

        observation.setId(qdmDataElement.get_id());
        observation.setSubject(createReference(fhirPatient));
        observation.setCode(convertToCodeSystems(converterBase.getCodeSystemEntriesService(), qdmDataElement.getDataElementCodes()));

        if (qdmDataElement.getResult() != null) {
            JsonNodeObservationResultProcessor resultProcessor =
                    new JsonNodeObservationResultProcessor(observation, converterBase.getCodeSystemEntriesService(), conversionMessages);

            resultProcessor.processNode(qdmDataElement.getResult());
        }

        if (qdmDataElement.getRelevantPeriod() != null) {
            observation.setEffective(createFhirPeriod(qdmDataElement.getRelevantPeriod()));
        }

        if (!converterBase.processNegation(qdmDataElement, observation)) {
            //http://hl7.org/fhir/us/qicore/qdm-to-qicore.html#8102-diagnostic-study-performed
            //Constrain status to -  final, amended, corrected, appended
            observation.setStatus(Observation.ObservationStatus.UNKNOWN);
            conversionMessages.add(NO_STATUS_MAPPING);
        }

        observation.setIssued(qdmDataElement.getAuthorDatetime());

        return QdmToFhirConversionResult.<Observation>builder()
                .fhirResource(observation)
                .conversionMessages(conversionMessages)
                .build();
    }

}
