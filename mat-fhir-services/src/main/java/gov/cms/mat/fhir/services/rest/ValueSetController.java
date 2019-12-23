package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.commons.objects.FhirResourceValidationResult;
import gov.cms.mat.fhir.commons.objects.TranslationOutcome;
import gov.cms.mat.fhir.rest.cql.ConversionType;
import gov.cms.mat.fhir.rest.cql.FhirValidationResult;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.components.mongo.ConversionResult;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.exceptions.ValueSetConversionException;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.rest.support.FhirValidatorProcessor;
import gov.cms.mat.fhir.services.service.ValueSetService;
import gov.cms.mat.fhir.services.summary.FhirValueSetResourceValidationResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/valueSet")
@Tag(name = "ValueSet-Controller", description = "API for converting MAT ValueSets to FHIR.")
@Slf4j
public class ValueSetController implements FhirValidatorProcessor {

    private final HapiFhirServer hapiFhirServer;
    private final ValueSetService valueSetService;

    public ValueSetController(HapiFhirServer hapiFhirServer,
                              ValueSetService valueSetService) {
        this.hapiFhirServer = hapiFhirServer;
        this.valueSetService = valueSetService;
    }

    @Operation(summary = "Translate all ValueSets in MAT to FHIR.",
            description = "Translate all the ValueSets in the MAT Database and persist to the HAPI FHIR Database.")
    @Transactional(readOnly = true)
    @PutMapping(path = "/translateAll")
    public TranslationOutcome translateAll(
            @RequestParam(required = false, defaultValue = "SIMPLE") XmlSource xmlSource,
            @RequestParam(required = false, defaultValue = "CONVERSION") ConversionType conversionType) {
        String successMessage = valueSetService.translateAll(xmlSource, conversionType);

        log.info(successMessage);

        return createOutcome(successMessage);
    }

    @Operation(summary = "Validate ValueSet conversion MAT to FHIR.",
            description = "Validate ValueSet conversion MAT to FHIR of all the ValueSets that are contained in the " +
                    "measure indicated by measureId.")
    @Transactional(readOnly = true)
    @PutMapping(path = "/validate")
    public FhirValueSetResourceValidationResult validate(
            @RequestParam(required = false, defaultValue = "SIMPLE") XmlSource xmlSource,
            @RequestParam String measureId) {

        List<ValueSet> valueSets = valueSetService.findValueSets(xmlSource, measureId, ConversionType.VALIDATION);

        if (valueSets.isEmpty()) {
            throw new ValueSetConversionException("No value sets found");
        } else {
            return generateValidationResults(valueSets, xmlSource, measureId);
        }
    }

    public FhirValueSetResourceValidationResult generateValidationResults(List<ValueSet> valueSets,
                                                                          XmlSource xmlSource,
                                                                          String measureId) {

        FhirValueSetResourceValidationResult response = new FhirValueSetResourceValidationResult();

        List<FhirResourceValidationResult> results = valueSets.stream()
                .map(v -> createResult(v, measureId))
                .collect(Collectors.toList());
        response.setFhirResourceValidationResults(results);

        ConversionResult conversionResult = ConversionReporter.getConversionResult();

        response.setValueSetConversionType(conversionResult.getValueSetConversionResults().getValueSetConversionType());
        response.setValueSetResults(conversionResult.getValueSetConversionResults().getValueSetResults());
        response.setXmlSource(xmlSource);

        return response;
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

    @Operation(summary = "Count of persisted FHIR ValueSets.",
            description = "The count of all the ValueSets in the HAPI FHIR Database.")
    @GetMapping(path = "/count")
    public int countValueSets() {
        return valueSetService.count();
    }

    @Operation(summary = "Delete all persisted FHIR ValueSets.",
            description = "Delete all the ValueSets in the HAPI FHIR Database.")
    @DeleteMapping(path = "/deleteAll")
    public int deleteValueSets() {
        return valueSetService.deleteAll();
    }

    private TranslationOutcome createOutcome(String successMessage) {
        TranslationOutcome res = new TranslationOutcome();
        res.setMessage(successMessage);
        res.setSuccessful(true);
        return res;
    }
}