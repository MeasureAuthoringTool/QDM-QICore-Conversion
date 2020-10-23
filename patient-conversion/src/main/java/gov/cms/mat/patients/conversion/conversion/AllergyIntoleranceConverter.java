package gov.cms.mat.patients.conversion.conversion;

import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.patients.conversion.conversion.results.QdmToFhirConversionResult;
import gov.cms.mat.patients.conversion.dao.QdmDataElement;
import gov.cms.mat.patients.conversion.service.CodeSystemEntriesService;
import gov.cms.mat.patients.conversion.service.ValidationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class AllergyIntoleranceConverter extends ConverterBase<AllergyIntolerance> {
    public static final String QDM_TYPE = "QDM::AllergyIntolerance";

    public AllergyIntoleranceConverter(CodeSystemEntriesService codeSystemEntriesService,
                                       FhirContext fhirContext,
                                       ObjectMapper objectMapper,
                                       ValidationService validationService) {
        super(codeSystemEntriesService, fhirContext, objectMapper, validationService);
    }

    @Override
    String getQdmType() {
        return QDM_TYPE;
    }

    @Override
    QdmToFhirConversionResult<AllergyIntolerance> convertToFhir(Patient fhirPatient, QdmDataElement qdmDataElement) {

        AllergyIntolerance allergyIntolerance = new AllergyIntolerance();
        allergyIntolerance.setId(qdmDataElement.get_id());

        // http://hl7.org/fhir/us/qicore/qdm-to-qicore.html#83-allergyintolerance
        // active, inactive, resolved
        // allergyIntolerance.setClinicalStatus() 	this is codeable Concept

        allergyIntolerance.setPatient(createReference(fhirPatient));

        if (CollectionUtils.isNotEmpty(qdmDataElement.getDataElementCodes())) {
            allergyIntolerance.setCode(convertToCodeSystems(codeSystemEntriesService, qdmDataElement.getDataElementCodes()));
        }

        if (qdmDataElement.getPrevalencePeriod() != null) {
            allergyIntolerance.setOnset(createFhirPeriod(qdmDataElement.getPrevalencePeriod()));
        }

        if (qdmDataElement.getType() != null) {
            List<AllergyIntolerance.AllergyIntoleranceReactionComponent> list = allergyIntolerance.getReaction();

            var component = new AllergyIntolerance.AllergyIntoleranceReactionComponent();
            component.setSubstance(convertToCodeableConcept(codeSystemEntriesService, qdmDataElement.getType()));
            list.add(component);
        }

        processNegation(qdmDataElement, allergyIntolerance);

        return QdmToFhirConversionResult.<AllergyIntolerance>builder()
                .fhirResource(allergyIntolerance)
                .conversionMessages(Collections.emptyList())
                .build();

    }
}
