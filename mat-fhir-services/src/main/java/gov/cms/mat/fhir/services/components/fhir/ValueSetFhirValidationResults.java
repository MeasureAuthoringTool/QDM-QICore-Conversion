package gov.cms.mat.fhir.services.components.fhir;

import gov.cms.mat.fhir.commons.objects.FhirResourceValidationResult;
import gov.cms.mat.fhir.rest.dto.FhirValidationResult;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.components.mongo.ConversionResult;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.exceptions.ValueSetValidationException;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.rest.support.FhirValidatorProcessor;
import gov.cms.mat.fhir.services.summary.FhirValueSetResourceValidationResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class ValueSetFhirValidationResults implements FhirValidatorProcessor {
    private final HapiFhirServer hapiFhirServer;

    public ValueSetFhirValidationResults(HapiFhirServer hapiFhirServer) {
        this.hapiFhirServer = hapiFhirServer;
    }

    public FhirValueSetResourceValidationResult generate(List<ValueSet> valueSets,
                                                         XmlSource xmlSource,
                                                         String measureId) {
        if (CollectionUtils.isEmpty(valueSets)) {
            throw new ValueSetValidationException(measureId);
        } else {
            return generateResponse(xmlSource);
        }
    }

    public FhirValueSetResourceValidationResult generateResponse(XmlSource xmlSource) {
        FhirValueSetResourceValidationResult response = new FhirValueSetResourceValidationResult();

        ConversionResult conversionResult = ConversionReporter.getConversionResult();
        response.setValueSetConversionResults(conversionResult.getValueSetConversionResults());

        response.setXmlSource(xmlSource);

        return response;
    }

    public void collectResults(List<ValueSet> valueSets, String measureId) {
        valueSets.forEach(v -> createResult(v, measureId));
    }

    private void createResult(ValueSet valueSet, String measureId) {
        log.debug("Validating oid: {} for measureId: {}", valueSet.getId(), measureId);
        FhirResourceValidationResult res = new FhirResourceValidationResult();

        validateResource(res, valueSet, hapiFhirServer.getCtx());

        res.setId(valueSet.getId());
        res.setType("ValueSet");

        res.setMeasureId(measureId);

        List<FhirValidationResult> results = buildResultList(res);
        ConversionReporter.setValueSetsValidationResults(res.getId(), results);
    }

    private List<FhirValidationResult> buildResultList(FhirResourceValidationResult res) {
        if (CollectionUtils.isEmpty(res.getValidationErrorList())) {
            return Collections.emptyList();
        } else {
            return buildResults(res);
        }
    }
}
