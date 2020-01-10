package gov.cms.mat.fhir.services.translate;

import gov.cms.mat.fhir.services.components.mat.MatXmlConverter;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.exceptions.HapiValueSetException;
import gov.cms.mat.fhir.services.exceptions.ValueSetValidationException;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.service.VsacService;
import gov.cms.mat.fhir.services.translate.creators.FhirRemover;
import gov.cms.mat.fhir.services.translate.creators.FhirValueSetCreator;
import lombok.extern.slf4j.Slf4j;
import mat.model.VSACValueSetWrapper;
import mat.model.cql.CQLQualityDataModelWrapper;
import mat.model.cql.CQLQualityDataSetDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ValueSetMapper implements FhirValueSetCreator, FhirRemover {
    private final VsacService vsacService;
    private final MatXmlConverter matXmlConverter;
    private final HapiFhirServer hapiFhirServer;

    public ValueSetMapper(VsacService vsacService, MatXmlConverter matXmlConverter, HapiFhirServer hapiFhirServer) {
        this.vsacService = vsacService;
        this.matXmlConverter = matXmlConverter;
        this.hapiFhirServer = hapiFhirServer;
    }

    public int count() {
        return hapiFhirServer.count(ValueSet.class);
    }

    public List<ValueSet> translateToFhir(String xml) {
        CQLQualityDataModelWrapper wrapper = matXmlConverter.toQualityData(xml);

        if (wrapper == null || CollectionUtils.isEmpty(wrapper.getQualityDataDTO())) {
            return Collections.emptyList();
        }

        List<ValueSet> valueSets = new ArrayList<>();

        wrapper.getQualityDataDTO()
                .stream()
                .filter(w -> !inHapi(w.getOid()))
                .forEach(t -> processFhir(t, valueSets));

        return valueSets;
    }

    private boolean inHapi(String oid) {
        Optional<String> optional = hapiFhirServer.fetchHapiLink(oid);

        if (optional.isPresent()) {
            log.debug("ValueSet {} is in hapi: {}", oid, optional.get());
            ConversionReporter.setValueSetsValidationLink(oid, optional.get(), "Exists");
            return true;
        } else {
            log.debug("ValueSet {} is NOT in hapi", oid);
            ConversionReporter.setValueSetsValidationError(oid, "ValueSet is NOT in hapi");
            return false;
        }
    }

    private void processFhir(CQLQualityDataSetDTO cqlQualityDataSetDTO,
                             List<ValueSet> valueSets) {
        String oid;

        if (StringUtils.isBlank(cqlQualityDataSetDTO.getOid())) {
            throw new ValueSetValidationException("missing oid");
        } else {
            oid = cqlQualityDataSetDTO.getOid();
        }

        VSACValueSetWrapper vsacValueSetWrapper = vsacService.getData(oid);

        if (vsacValueSetWrapper == null) {
            log.debug("VsacService returned null for oid: {}", oid);
            ConversionReporter.setValueSetInit(oid, "Not Found in VSAC");
        } else {
            List<ValueSet> valueSetsCreated = createFhirValueSetList(cqlQualityDataSetDTO, vsacValueSetWrapper);
            valueSets.addAll(valueSetsCreated);
            ConversionReporter.setValueSetInit(oid, "Found in VSAC");
        }
    }

    private List<ValueSet> createFhirValueSetList(CQLQualityDataSetDTO cqlQualityDataSetDTO,
                                                  VSACValueSetWrapper vsacValueSet) {
        return vsacValueSet.getValueSetList()
                .stream()
                .map(matValueSet -> createFhirValueSet(matValueSet, cqlQualityDataSetDTO))
                .collect(Collectors.toList());
    }

    public ValueSet persistFhirValueSet(ValueSet valueSet) {
        Bundle bundle = hapiFhirServer.createAndExecuteBundle(valueSet);

        if (bundle.isEmpty()) {
            throw new HapiValueSetException(valueSet.getId());
        } else {
            return (ValueSet) bundle.getEntry().get(0).getResource();
        }
    }

    public int deleteAll() {
        return deleteAllResource(hapiFhirServer, ValueSet.class);
    }
}

