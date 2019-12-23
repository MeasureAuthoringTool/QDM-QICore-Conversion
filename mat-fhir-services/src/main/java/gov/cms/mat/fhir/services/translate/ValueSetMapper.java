package gov.cms.mat.fhir.services.translate;

import gov.cms.mat.fhir.rest.cql.ConversionType;
import gov.cms.mat.fhir.services.components.mat.MatXmlConverter;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.service.VsacService;
import gov.cms.mat.fhir.services.translate.creators.FhirValueSetCreator;
import lombok.extern.slf4j.Slf4j;
import mat.model.MatValueSet;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.hl7.fhir.instance.model.api.IBaseBundle.LINK_NEXT;

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

    public int count() {
        return hapiFhirServer.count(ValueSet.class);
    }

    public List<ValueSet> translateToFhir(String xml, ConversionType conversionType) {
        CQLQualityDataModelWrapper wrapper = matXmlConverter.toQualityData(xml);

        if (wrapper == null || CollectionUtils.isEmpty(wrapper.getQualityDataDTO())) {
            return Collections.emptyList();
        }

        List<ValueSet> valueSets = new ArrayList<>();

        wrapper.getQualityDataDTO()
                .forEach(t -> processFhir(t, valueSets, conversionType));

        return valueSets;
    }

    private void processFhir(CQLQualityDataSetDTO cqlQualityDataSetDTO,
                             List<ValueSet> valueSets,
                             ConversionType conversionType) {
        String oid;

        if (StringUtils.isBlank(cqlQualityDataSetDTO.getOid())) {
            return;
        } else {
            oid = cqlQualityDataSetDTO.getOid();
        }

        Bundle hapiBundle = hapiFhirServer.isValueSetInHapi(oid);

        if (conversionType == ConversionType.CONVERSION && hapiBundle != null && hapiBundle.hasEntry()) {
            log.debug("Fhir valueSet already in hapi, oid: {}", oid);
            ConversionReporter.setValueSetSuccessResult(oid);
        } else {
            ConversionReporter.resetValueSetResults(conversionType);

            VSACValueSetWrapper vsacValueSetWrapper = vsacService.getData(oid);

            if (vsacValueSetWrapper == null) {
                log.debug("VsacService returned null for oid: {}", oid);
                ConversionReporter.setValueSetFailResult(oid, "Not Found in VSAC");
            } else {
                List<ValueSet> valueSetsCreated = createFhirValueSetList(cqlQualityDataSetDTO, vsacValueSetWrapper, conversionType);
                valueSets.addAll(valueSetsCreated);
                ConversionReporter.setValueSetSuccessResult(oid);
            }
        }
    }

    private List<ValueSet> createFhirValueSetList(CQLQualityDataSetDTO cqlQualityDataSetDTO,
                                                  VSACValueSetWrapper vsacValueSet,
                                                  ConversionType conversionType) {
        return vsacValueSet.getValueSetList()
                .stream()
                .map(matValueSet -> createAndPersistFhirValueSet(matValueSet, cqlQualityDataSetDTO, conversionType))
                .collect(Collectors.toList());
    }

    private ValueSet createAndPersistFhirValueSet(MatValueSet matValueSet,
                                                  CQLQualityDataSetDTO cqlQualityDataSetDTO,
                                                  ConversionType conversionType) {
        ValueSet valueSet = createFhirValueSet(matValueSet, cqlQualityDataSetDTO);

        if (conversionType.equals(ConversionType.VALIDATION)) {
            return valueSet;
        } else {
            Bundle bundle = hapiFhirServer.createAndExecuteBundle(valueSet);

            if (bundle.isEmpty()) {
                throw new IllegalArgumentException("Could not create hapi value set with oid: " + matValueSet.getID());
            } else {
                return (ValueSet) bundle.getEntry().get(0).getResource();
            }
        }
    }

    public int deleteAll() {
        Bundle bundle = hapiFhirServer.getAll(ValueSet.class);

        AtomicInteger count = new AtomicInteger();

        while (bundle.hasEntry()) {
            bundle.getEntry().forEach(f -> {
                count.getAndIncrement();
                hapiFhirServer.delete(f.getResource());
            });

            if (bundle.getLink(LINK_NEXT) != null) {
                // load next page
                bundle = hapiFhirServer.getNextPage(bundle);
            } else {
                break;
            }
        }

        return count.get();
    }
}

