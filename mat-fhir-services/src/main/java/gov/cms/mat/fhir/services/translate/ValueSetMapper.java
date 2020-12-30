package gov.cms.mat.fhir.services.translate;

import gov.cms.mat.fhir.services.components.mat.MatXmlConverter;
import gov.cms.mat.vsac.VsacService;
import gov.cms.mat.vsac.model.*;
import gov.cms.mat.vsac.util.VsacConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ValueSetMapper {
    private final VsacService vsacService;
    private final MatXmlConverter matXmlConverter;
    private final VsacConverter vsacConverter = new VsacConverter();

    public ValueSetMapper(VsacService vsacService, MatXmlConverter matXmlConverter) {
        this.vsacService = vsacService;
        this.matXmlConverter = matXmlConverter;
    }

    public ValueSet mapToFhir(String ticketGrantingTicket, String oid, String version) {
        ValueSetResult valueSetResult = vsacService.getValueSetResult(oid, ticketGrantingTicket);
        if (valueSetResult.isFailResponse()) {
            throw new RuntimeException(valueSetResult.getFailReason() +" oid=" + oid + (version == null ? "|" + version : ""));
        } else {
            ValueSetWrapper vsacWrapper = vsacConverter.toWrapper(valueSetResult.getXmlPayLoad());
            VsacValueSet vsacMostRecent = vsacWrapper.getVsacValueSetList().get(0);
            return createFhirValueSet(vsacMostRecent);
        }
    }

    private ValueSet buildValueSet(VsacValueSet vsacValueSet) {
        ValueSet result = new ValueSet();
        result.setId(vsacValueSet.getID());
        result.setUrl("http://cts.nlm.nih.gov/fhir/ValueSet/" + result.getId());

        if (StringUtils.isNotEmpty(vsacValueSet.getStatus())) {
            result.setStatus(Enumerations.PublicationStatus.fromCode(vsacValueSet.getStatus().toLowerCase()));
        }

        return result.setIdentifier(Collections.singletonList(createIdentifier("urn:ietf:rfc:3986", vsacValueSet.getID())))
                .setName(vsacValueSet.getDisplayName())
                .setPublisher(vsacValueSet.getSource());
    }

    private Identifier createIdentifier(String system, String value) {
        return new Identifier()
                .setSystem(system)
                .setValue(value);
    }

    private ValueSet createFhirValueSet(VsacValueSet matValueSet) {
        ValueSet valueSet = buildValueSet(matValueSet);

        processCompose(matValueSet, valueSet);

        return valueSet;
    }

    private void processCompose(VsacValueSet vsacValueSet, ValueSet fhirValueSet) {
        ValueSet.ValueSetComposeComponent compose = new ValueSet.ValueSetComposeComponent();
        compose.setInclude(new ArrayList<>());
        fhirValueSet.setCompose(compose);
        compose.getInclude().add(createConceptSetComponent(getConceptListSafely(vsacValueSet.getConceptList())));
    }

    private List<MatConcept> getConceptListSafely(MatConceptList conceptList) {
        if (conceptList == null || CollectionUtils.isEmpty(conceptList.getConceptList())) {
            return Collections.emptyList();
        } else {
            return conceptList.getConceptList();
        }
    }

    private ValueSet.ConceptSetComponent createConceptSetComponent(List<MatConcept> matConceptList) {
        ValueSet.ConceptSetComponent fhirComponent = new ValueSet.ConceptSetComponent();

        fhirComponent.setVersion(matConceptList.get(0).getCodeSystemVersion());
        fhirComponent.setSystem(matConceptList.get(0).getCodeSystem());

        fhirComponent.setConcept(createConceptReferenceComponents(matConceptList));
        return fhirComponent;
    }

    private List<ValueSet.ConceptReferenceComponent> createConceptReferenceComponents(List<MatConcept> matConceptList) {
        return matConceptList.stream()
                .map(this::createConceptSetComponent)
                .collect(Collectors.toList());
    }

    private ValueSet.ConceptReferenceComponent createConceptSetComponent(MatConcept matConcept) {
        return new ValueSet.ConceptReferenceComponent()
                .setCode(matConcept.getCode())
                .setDisplay(matConcept.getDisplayName());
    }
}

