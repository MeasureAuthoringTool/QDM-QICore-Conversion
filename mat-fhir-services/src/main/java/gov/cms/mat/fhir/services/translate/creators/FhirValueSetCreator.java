package gov.cms.mat.fhir.services.translate.creators;

import mat.model.MatConcept;
import mat.model.MatConceptList;
import mat.model.MatValueSet;
import mat.model.cql.CQLQualityDataSetDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.ValueSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface FhirValueSetCreator extends FhirCreator {


    default ValueSet createFhirValueSet(MatValueSet matValueSet, CQLQualityDataSetDTO cqlQualityDataSetDTO) {
        ValueSet valueSet = buildValueSet(matValueSet, cqlQualityDataSetDTO);

        processCompose(matValueSet, valueSet);

        return valueSet;
    }

    default void processCompose(MatValueSet matValueSet, ValueSet valueSet) {
        ValueSet.ValueSetComposeComponent compose = new ValueSet.ValueSetComposeComponent();
        compose.setInclude(new ArrayList<>());
        valueSet.setCompose(compose);

        Map<String, List<MatConcept>> map = createMatConceptMap(getConceptListSafely(matValueSet.getConceptList()));
        map.values()
                .forEach(list -> compose.getInclude().add(createConceptSetComponent(list)));
    }

    default ValueSet buildValueSet(MatValueSet matValueSet, CQLQualityDataSetDTO cqlQualityDataSetDTO) {
        ValueSet valueSet = new ValueSet();
        valueSet.setId(matValueSet.getID());

        if (StringUtils.isNotEmpty(matValueSet.getStatus())) {
            valueSet.setStatus(Enumerations.PublicationStatus.fromCode(matValueSet.getStatus().toLowerCase()));
        }

        return valueSet.setIdentifier(Collections.singletonList(createIdentifier("urn:ietf:rfc:3986", matValueSet.getID())))
                .setVersion(cqlQualityDataSetDTO.getVersion())
                .setName(matValueSet.getDisplayName())
                .setPublisher(matValueSet.getSource());
    }

    default List<MatConcept> getConceptListSafely(MatConceptList conceptList) {
        if (conceptList == null || CollectionUtils.isEmpty(conceptList.getConceptList())) {
            return Collections.emptyList();
        } else {
            return conceptList.getConceptList();
        }
    }


    default ValueSet.ConceptSetComponent createConceptSetComponent(List<MatConcept> matConceptList) {
        ValueSet.ConceptSetComponent fhirComponent = new ValueSet.ConceptSetComponent();

        fhirComponent.setVersion(matConceptList.get(0).getCodeSystemVersion());
        fhirComponent.setSystem(matConceptList.get(0).getCodeSystem());

        fhirComponent.setConcept(createConceptReferenceComponents(matConceptList));
        return fhirComponent;
    }

    default List<ValueSet.ConceptReferenceComponent> createConceptReferenceComponents(List<MatConcept> matConceptList) {
        return matConceptList.stream()
                .map(this::createConceptSetComponent)
                .collect(Collectors.toList());
    }

    default Map<String, List<MatConcept>> createMatConceptMap(List<MatConcept> conceptList) {
        return conceptList.stream()
                .collect(Collectors.groupingBy(MatConcept::createKey));
    }

    default ValueSet.ConceptReferenceComponent createConceptSetComponent(MatConcept matConcept) {
        return new ValueSet.ConceptReferenceComponent()
                .setCode(matConcept.getCode())
                .setDisplay(matConcept.getDisplayName());
    }
}
