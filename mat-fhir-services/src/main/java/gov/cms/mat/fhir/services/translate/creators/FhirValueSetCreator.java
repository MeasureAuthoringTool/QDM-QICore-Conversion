package gov.cms.mat.fhir.services.translate.creators;

import mat.model.MatConcept;
import mat.model.MatConceptList;
import mat.model.MatValueSet;
import mat.model.VSACValueSetWrapper;
import mat.model.cql.CQLQualityDataSetDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.ValueSet;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public interface FhirValueSetCreator extends FhirCreator {
    String SYSTEM_IDENTIFIER = "urn:ietf:rfc:3986";

    default List<ValueSet> createFhirValueSetList(CQLQualityDataSetDTO cqlQualityDataSetDTO, VSACValueSetWrapper vsacValueSet) {
        return vsacValueSet.getValueSetList()
                .stream()
                .map(matValueSet -> createFhirValueSet(matValueSet, cqlQualityDataSetDTO))
                .collect(Collectors.toList());
    }

   default ValueSet createFhirValueSet(MatValueSet matValueSet, CQLQualityDataSetDTO cqlQualityDataSetDTO) {
        ValueSet valueSet = new ValueSet();
        valueSet.setId(matValueSet.getID());
        valueSet.setIdentifier(Collections.singletonList(createIdentifier(SYSTEM_IDENTIFIER, matValueSet.getID())));
        valueSet.setVersion(matValueSet.getVersion());
        valueSet.setName(matValueSet.getDisplayName());
         // valueSet.setTitle()   //todo DU cannot find

        //todo DU can throw exception if not found
        valueSet.setStatus(Enumerations.PublicationStatus.fromCode(matValueSet.getStatus()));
        valueSet.setPublisher(matValueSet.getSource()); //todo DU is this correct

        ValueSet.ValueSetComposeComponent value = new ValueSet.ValueSetComposeComponent();
        value.setInclude(create(matValueSet.getConceptList()));

        valueSet.setCompose(value);
        return valueSet;

    }

    default List<ValueSet.ConceptSetComponent> create(MatConceptList conceptList) {
        if (conceptList == null || CollectionUtils.isEmpty(conceptList.getConceptList())) {
            return Collections.emptyList();
        } else {
            ValueSet.ConceptSetComponent fhirComponent = new ValueSet.ConceptSetComponent();
            // fhirComponent.setVersion() todo DU ??
            //  fhirComponent.setSystem()

            List<ValueSet.ConceptReferenceComponent> list = conceptList.getConceptList().stream()
                    .map(this::createConceptSetComponent)
                    .collect(Collectors.toList());

            fhirComponent.setConcept(list);

            return Collections.singletonList(fhirComponent);
        }

    }

    default ValueSet.ConceptReferenceComponent createConceptSetComponent(MatConcept matConcept) {
        ValueSet.ConceptReferenceComponent component = new ValueSet.ConceptReferenceComponent();

        component.setCode(matConcept.getCode());
        component.setDisplay(matConcept.getDisplayName());

        return component;
    }
}
