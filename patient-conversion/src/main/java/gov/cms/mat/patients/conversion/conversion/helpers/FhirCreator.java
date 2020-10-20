package gov.cms.mat.patients.conversion.conversion.helpers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationOptions;
import ca.uhn.fhir.validation.ValidationResult;
import gov.cms.mat.patients.conversion.dao.QdmCodeSystem;
import gov.cms.mat.patients.conversion.dao.QdmPeriod;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.hapi.ctx.IValidationSupport;
import org.hl7.fhir.r4.hapi.validation.FhirInstanceValidator;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public interface FhirCreator {
    default Reference createReference(Patient fhirPatient) {
        Reference reference = new Reference("Patient/" + fhirPatient.getId());
        return reference.setDisplay(convertHumanNamesToString(fhirPatient.getName()));
    }

    default CodeableConcept createCodeableConcept(QdmCodeSystem code, String system) {
        return createCodeableConcept(system, code.getCode(), code.getDisplay());
    }

    default CodeableConcept createCodeableConcept(String system, String code, String display) {
        return new CodeableConcept()
                .setCoding(Collections.singletonList(new Coding(system, code, display)));
    }

    default String convertHumanNamesToString(List<HumanName> humanNames) {
        if (CollectionUtils.isEmpty(humanNames)) {
            return "No Human Names Found";
        } else {
            HumanName humanName = humanNames.get(0);

            String given = humanName.getGiven().stream()
                    .map(StringType::getValueNotNull)
                    .collect(Collectors.joining(" "));

            return given + " " + humanName.getFamily();
        }
    }

    default Period createFhirPeriod(QdmPeriod relevantPeriod) {
        if (relevantPeriod == null) {
            return null;
        } else {
            return new Period()
                    .setStart(relevantPeriod.getLow())
                    .setEnd(relevantPeriod.getHigh() == null ? null : relevantPeriod.getHigh());
        }
    }

    default String toJson(FhirContext fhirContext, IBaseResource theResource) {
        return fhirContext.newJsonParser()
                .setPrettyPrint(true)
                .encodeResourceToString(theResource);
    }
}
