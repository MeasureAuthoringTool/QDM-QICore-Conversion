package gov.cms.mat.fhir.services.translate;

import gov.cms.mat.fhir.services.components.mat.MatXmlConverter;
import gov.cms.mat.fhir.services.service.VsacService;
import gov.cms.mat.fhir.services.translate.creators.FhirValueSetCreator;
import lombok.extern.slf4j.Slf4j;
import mat.model.VSACValueSetWrapper;
import mat.model.cql.CQLQualityDataModelWrapper;
import mat.model.cql.CQLQualityDataSetDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class ValueSetMapper implements FhirValueSetCreator {
    private final VsacService vsacService;
    private final MatXmlConverter matXmlConverter;

    public ValueSetMapper(VsacService vsacService, MatXmlConverter matXmlConverter) {
        this.vsacService = vsacService;
        this.matXmlConverter = matXmlConverter;
    }

    public List<ValueSet> translateToFhir(String xml) {
        CQLQualityDataModelWrapper wrapper = matXmlConverter.toQualityData(xml);

        if (wrapper == null || CollectionUtils.isEmpty(wrapper.getQualityDataDTO())) {
            return Collections.emptyList();
        }

        Map<String, VSACValueSetWrapper> map = createOidMap(wrapper);

        List<ValueSet> valueSets = new ArrayList<>();

        wrapper.getQualityDataDTO()
                .forEach(t -> processFhir(t, map, valueSets));

        return valueSets;
    }

    private void processFhir(CQLQualityDataSetDTO cqlQualityDataSetDTO,
                             Map<String, VSACValueSetWrapper> map,
                             List<ValueSet> valueSets) {

        if (map.containsKey(cqlQualityDataSetDTO.getOid())) {
            VSACValueSetWrapper vsacValueSet = map.get(cqlQualityDataSetDTO.getOid());

            List<ValueSet> valueSetsCreated = createFhirValueSetList(cqlQualityDataSetDTO, vsacValueSet);
            valueSets.addAll(valueSetsCreated);

        } else {
            log.debug("No value in map for oid: {}", cqlQualityDataSetDTO.getOid());
        }
    }

    private Map<String, VSACValueSetWrapper> createOidMap(CQLQualityDataModelWrapper cqlQualityDataModelWrapper) {
        Map<String, VSACValueSetWrapper> map = new HashMap<>(cqlQualityDataModelWrapper.getQualityDataDTO().size());

        cqlQualityDataModelWrapper.getQualityDataDTO()
                .forEach(qualityDataSetDTO -> fetchDataFromVsac(qualityDataSetDTO.getOid(), map));

        return map;
    }

    private void fetchDataFromVsac(String oid, Map<String, VSACValueSetWrapper> map) {
        VSACValueSetWrapper vsacValueSetWrapper = vsacService.getData(oid);

        if (vsacValueSetWrapper == null) {
            log.debug("VsacService returned null for oid: {}", oid);
        } else {
            map.put(oid, vsacValueSetWrapper);
        }
    }
}

