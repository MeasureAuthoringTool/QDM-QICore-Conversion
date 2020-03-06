package gov.cms.mat.fhir.services.translate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.stereotype.Component;

import gov.cms.mat.fhir.services.components.mat.MatXmlConverter;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.exceptions.ValueSetValidationException;
import gov.cms.mat.fhir.services.hapi.HapiFhirLinkProcessor;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.service.VsacService;
import gov.cms.mat.fhir.services.translate.creators.FhirRemover;
import gov.cms.mat.fhir.services.translate.creators.FhirValueSetCreator;
import lombok.extern.slf4j.Slf4j;
import mat.model.VSACValueSetWrapper;
import mat.model.cql.CQLQualityDataModelWrapper;
import mat.model.cql.CQLQualityDataSetDTO;

import static gov.cms.mat.fhir.services.components.mongo.HapiResourcePersistedState.EXISTS;
import static gov.cms.mat.fhir.services.components.mongo.HapiResourcePersistedState.NEW;

@Component
@Slf4j
public class ValueSetMapper implements FhirValueSetCreator, FhirRemover {
    private final VsacService vsacService;
    private final MatXmlConverter matXmlConverter;
    private final HapiFhirServer hapiFhirServer;
    private final HapiFhirLinkProcessor hapiFhirLinkProcessor;

    public ValueSetMapper(VsacService vsacService,
                          MatXmlConverter matXmlConverter,
                          HapiFhirServer hapiFhirServer,
                          HapiFhirLinkProcessor hapiFhirLinkProcessor) {
        this.vsacService = vsacService;
        this.matXmlConverter = matXmlConverter;
        this.hapiFhirServer = hapiFhirServer;
        this.hapiFhirLinkProcessor = hapiFhirLinkProcessor;
    }

    public int count() {
        return hapiFhirServer.count(ValueSet.class);
    }

    public List<ValueSet> translateToFhir(String xml, String vsacGrantingTicket) {
        CQLQualityDataModelWrapper wrapper = matXmlConverter.toQualityData(xml);

        if (wrapper == null || CollectionUtils.isEmpty(wrapper.getQualityDataDTO())) {
            return Collections.emptyList();
        }

        List<ValueSet> valueSets = new ArrayList<>();

        wrapper.getQualityDataDTO()
                .stream()
                .filter(w -> !inHapi(w.getOid()))
                .forEach(t -> processFhir(t, valueSets, vsacGrantingTicket));

        return valueSets;
    }

    private boolean inHapi(String oid) {
        String url = hapiFhirServer.buildHapiFhirUrl("ValueSet", oid);
        Optional<ValueSet> optional = hapiFhirLinkProcessor.fetchValueSetByUrl(url);

        if (optional.isPresent()) {
            log.debug("ValueSet {} is in hapi: {}", oid, optional.get());
            ConversionReporter.setValueSetsValidationLink(oid, optional.get().getUrl(), EXISTS);
            ConversionReporter.setValueSetJson(oid, hapiFhirServer.toJson(optional.get()));
            return true;
        } else {
            log.debug("ValueSet {} is NOT in hapi", oid);
            ConversionReporter.setValueSetsValidationError(oid, "ValueSet is NOT in hapi");
            return false;
        }
    }

    private void processFhir(CQLQualityDataSetDTO cqlQualityDataSetDTO,
                             List<ValueSet> valueSets,
                             String vsacGrantingTicket) {
        String oid;

        if (StringUtils.isBlank(cqlQualityDataSetDTO.getOid())) {
            throw new ValueSetValidationException("missing oid");
        } else {
            oid = cqlQualityDataSetDTO.getOid();
        }

        VSACValueSetWrapper vsacValueSetWrapper = vsacService.getData(oid, vsacGrantingTicket);

        if (vsacValueSetWrapper == null) {
            log.debug("VsacService returned null for oid: {}", oid);
            ConversionReporter.setValueSetInit(oid, "Not Found in VSAC", Boolean.FALSE);
        } else {
            List<ValueSet> valueSetsCreated = createFhirValueSetList(cqlQualityDataSetDTO, vsacValueSetWrapper);

            valueSetsCreated.forEach(this::addJsonToReport);

            valueSets.addAll(valueSetsCreated);

            ConversionReporter.setValueSetInit(oid, "Found in VSAC", null);
        }
    }

    private void addJsonToReport(ValueSet valueSet) {
        ConversionReporter.setValueSetsValidationLink(valueSet.getId(), null, NEW);
        ConversionReporter.setValueSetJson(valueSet.getId(), hapiFhirServer.toJson(valueSet));
    }

    private List<ValueSet> createFhirValueSetList(CQLQualityDataSetDTO cqlQualityDataSetDTO,
                                                  VSACValueSetWrapper vsacValueSet) {
        return vsacValueSet.getValueSetList()
                .stream()
                .map(matValueSet -> createFhirValueSet(matValueSet, cqlQualityDataSetDTO))
                .collect(Collectors.toList());
    }


    public int deleteAll() {
        return deleteAllResource(hapiFhirServer, ValueSet.class);
    }
}

