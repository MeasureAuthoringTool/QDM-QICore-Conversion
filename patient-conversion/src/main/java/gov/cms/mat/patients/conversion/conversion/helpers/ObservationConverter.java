package gov.cms.mat.patients.conversion.conversion.helpers;

import gov.cms.mat.patients.conversion.conversion.ConverterBase;
import gov.cms.mat.patients.conversion.conversion.results.QdmToFhirConversionResult;
import gov.cms.mat.patients.conversion.dao.QdmCodeSystem;
import gov.cms.mat.patients.conversion.dao.QdmComponent;
import gov.cms.mat.patients.conversion.dao.QdmDataElement;
import gov.cms.mat.patients.conversion.service.CodeSystemEntriesService;
import org.apache.commons.collections4.CollectionUtils;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
                    new JsonNodeObservationResultProcessor(converterBase.getCodeSystemEntriesService(), conversionMessages);

            observation.setValue(resultProcessor.findType(qdmDataElement.getResult()));
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

        if (qdmDataElement.getAuthorDatetime() != null) {
            observation.setIssued(qdmDataElement.getAuthorDatetime());
        } else {
            observation.setIssued(qdmDataElement.getResultDatetime());
        }

        if (CollectionUtils.isNotEmpty(qdmDataElement.getComponents())) {
            List<Observation.ObservationComponentComponent> fhirComponents = processComponents(qdmDataElement.getComponents(),
                    converterBase.getCodeSystemEntriesService(), conversionMessages);

            observation.setComponent(fhirComponents);
        }

        return QdmToFhirConversionResult.<Observation>builder()
                .fhirResource(observation)
                .conversionMessages(conversionMessages)
                .build();
    }

    private List<Observation.ObservationComponentComponent> processComponents(List<QdmComponent> qdmComponents,
                                                                              CodeSystemEntriesService codeSystemEntriesService,
                                                                              List<String> conversionMessages) {
        return qdmComponents.stream()
                .map(c -> convertComponent(c, codeSystemEntriesService, conversionMessages))
                .collect(Collectors.toList());
    }

    default Observation.ObservationComponentComponent convertComponent(QdmComponent qdmComponent,
                                                                       CodeSystemEntriesService codeSystemEntriesService,
                                                                       List<String> conversionMessages) {
        Observation.ObservationComponentComponent component = new Observation.ObservationComponentComponent();

        if (qdmComponent.getCode() != null) {
            QdmCodeSystem qdmCodeSystem = new QdmCodeSystem();
            qdmCodeSystem.setDisplay(qdmComponent.getCode().getDisplay());
            qdmCodeSystem.setCode(qdmComponent.getCode().getCode());
            qdmCodeSystem.setSystem(qdmCodeSystem.getCodeSystem());

            component.setCode(convertToCodeableConcept(codeSystemEntriesService, qdmCodeSystem));
        }


        if (qdmComponent.getResult() != null) {
            JsonNodeObservationResultProcessor resultProcessor =
                    new JsonNodeObservationResultProcessor(codeSystemEntriesService, conversionMessages);

            component.setValue(resultProcessor.findType(qdmComponent.getResult()));
        }

        return component;
    }
}
