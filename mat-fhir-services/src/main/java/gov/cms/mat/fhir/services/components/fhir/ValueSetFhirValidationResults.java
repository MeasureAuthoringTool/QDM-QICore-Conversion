package gov.cms.mat.fhir.services.components.fhir;

import gov.cms.mat.fhir.commons.objects.FhirResourceValidationResult;
import gov.cms.mat.fhir.rest.dto.FhirValidationResult;
import gov.cms.mat.fhir.rest.dto.ValueSetConversionResults;
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
import java.util.stream.Collectors;

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
            return generateResponse(valueSets, xmlSource, measureId);
        }
    }

    public FhirValueSetResourceValidationResult generateResponse(List<ValueSet> valueSets, XmlSource xmlSource, String measureId) {
        FhirValueSetResourceValidationResult response = new FhirValueSetResourceValidationResult();

        List<FhirResourceValidationResult> results = getCollect(valueSets, measureId);

        response.setFhirResourceValidationResults(results);

        ConversionResult conversionResult = ConversionReporter.getConversionResult();

        ValueSetConversionResults valueSetConversionResults = conversionResult.getValueSetConversionResults();

        response.setValueSetConversionType(valueSetConversionResults == null ? null :
                valueSetConversionResults.getValueSetConversionType());
        response.setValueSetResults(valueSetConversionResults == null ? null :
                valueSetConversionResults.getValueSetResults());

        response.setXmlSource(xmlSource);

        return response;
    }

    public List<FhirResourceValidationResult> getCollect(List<ValueSet> valueSets, String measureId) {
        return valueSets.stream()
                .map(v -> createResult(v, measureId))
                .collect(Collectors.toList());
    }

    private FhirResourceValidationResult createResult(ValueSet valueSet, String measureId) {
        FhirResourceValidationResult res = new FhirResourceValidationResult();
        validateResource(res, valueSet, hapiFhirServer.getCtx());

        res.setId(valueSet.getId());
        res.setType("ValueSet");
        res.setMeasureId(measureId);

        List<FhirValidationResult> results = buildResultList(res);
        ConversionReporter.setValueSetsValidationResults(res.getId(), results);

        return res;
    }

    private List<FhirValidationResult> buildResultList(FhirResourceValidationResult res) {
        if (CollectionUtils.isEmpty(res.getValidationErrorList())) {
            return Collections.emptyList();
        } else {
            return buildResults(res);
        }
    }
}
