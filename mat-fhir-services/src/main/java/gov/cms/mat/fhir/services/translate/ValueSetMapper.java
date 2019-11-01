package gov.cms.mat.fhir.services.translate;

import ca.uhn.fhir.rest.api.MethodOutcome;
import gov.cms.mat.fhir.services.components.mat.MatXmlConverter;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.service.VsacService;
import gov.cms.mat.fhir.services.translate.creators.FhirValueSetCreator;
import lombok.extern.slf4j.Slf4j;
import mat.model.MatValueSet;
import mat.model.VSACValueSetWrapper;
import mat.model.cql.CQLQualityDataModelWrapper;
import mat.model.cql.CQLQualityDataSetDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ValueSetMapper implements FhirValueSetCreator {
    private final VsacService vsacService;
    private final MatXmlConverter matXmlConverter;
    private final HapiFhirServer hapiFhirServer;

    public ValueSetMapper(VsacService vsacService, MatXmlConverter matXmlConverter, HapiFhirServer hapiFhirServer) {
        this.vsacService = vsacService;
        this.matXmlConverter = matXmlConverter;
        this.hapiFhirServer = hapiFhirServer;
    }

    public List<ValueSet> translateToFhir(String xml) {
        CQLQualityDataModelWrapper wrapper = matXmlConverter.toQualityData(xml);

        if (wrapper == null || CollectionUtils.isEmpty(wrapper.getQualityDataDTO())) {
            return Collections.emptyList();
        }

        List<ValueSet> valueSets = new ArrayList<>();

        wrapper.getQualityDataDTO()
                .forEach(t -> processFhir(t, valueSets));

        return valueSets;
    }

    private void processFhir(CQLQualityDataSetDTO cqlQualityDataSetDTO,
                             List<ValueSet> valueSets) {
        String oid;

        if (StringUtils.isBlank(cqlQualityDataSetDTO.getOid())) {
            return;
        } else {
            oid = cqlQualityDataSetDTO.getOid();
        }

        Bundle hapiBundle = isInHapi(oid);

        if (hapiBundle != null && hapiBundle.hasEntry()) {
            ValueSet valueSet = (ValueSet) hapiBundle.getEntry().get(0).getResource();
            valueSets.add(valueSet);
        } else {
            VSACValueSetWrapper vsacValueSetWrapper = vsacService.getData(oid);

            if (vsacValueSetWrapper == null) {
                log.debug("VsacService returned null for oid: {}", oid);
                return;
            }

            List<ValueSet> valueSetsCreated = createFhirValueSetList(cqlQualityDataSetDTO, vsacValueSetWrapper);
            valueSets.addAll(valueSetsCreated);
        }
    }

    private List<ValueSet> createFhirValueSetList(CQLQualityDataSetDTO cqlQualityDataSetDTO,
                                                  VSACValueSetWrapper vsacValueSet) {
        return vsacValueSet.getValueSetList()
                .stream()
                .map(matValueSet -> createAndPersistFhirValueSet(matValueSet, cqlQualityDataSetDTO))
                .collect(Collectors.toList());
    }

    private ValueSet createAndPersistFhirValueSet(MatValueSet matValueSet, CQLQualityDataSetDTO cqlQualityDataSetDTO) {
        ValueSet valueSet = createFhirValueSet(matValueSet, cqlQualityDataSetDTO);

        MethodOutcome methodOutcome = hapiFhirServer.create(valueSet);

        if (BooleanUtils.isTrue(methodOutcome.getCreated())) {
            return (ValueSet) methodOutcome.getResource();
        } else {
            throw new IllegalArgumentException("oops");
        }
    }


    private Bundle isInHapi(String oid) {
        return hapiFhirServer.getHapiClient().search()
                .forResource(ValueSet.class)
                .where(ValueSet.IDENTIFIER.exactly().systemAndCode(SYSTEM_IDENTIFIER, oid))
                .returnBundle(Bundle.class)
                .execute();
    }

}

