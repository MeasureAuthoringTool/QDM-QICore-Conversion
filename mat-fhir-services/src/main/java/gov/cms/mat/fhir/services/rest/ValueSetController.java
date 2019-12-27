package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.commons.objects.TranslationOutcome;
import gov.cms.mat.fhir.rest.dto.ConversionType;
import gov.cms.mat.fhir.services.components.fhir.ValueSetFhirValidationResults;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.exceptions.ValueSetConversionException;
import gov.cms.mat.fhir.services.rest.support.FhirValidatorProcessor;
import gov.cms.mat.fhir.services.service.ValueSetService;
import gov.cms.mat.fhir.services.summary.FhirValueSetResourceValidationResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/valueSet")
@Tag(name = "ValueSet-Controller", description = "API for converting MAT ValueSets to FHIR.")
@Slf4j
public class ValueSetController implements FhirValidatorProcessor {
    private final ValueSetService valueSetService;
    private final ValueSetFhirValidationResults valueSetFhirValidationResults;

    public ValueSetController(ValueSetService valueSetService,
                              ValueSetFhirValidationResults valueSetFhirValidationResults) {
        this.valueSetService = valueSetService;
        this.valueSetFhirValidationResults = valueSetFhirValidationResults;
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
            return valueSetFhirValidationResults.generate(valueSets, xmlSource, measureId);
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