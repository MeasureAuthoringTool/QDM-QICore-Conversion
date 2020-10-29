package gov.cms.mat.patients.conversion.conversion;


import ca.uhn.fhir.validation.ValidationResult;
import gov.cms.mat.patients.conversion.conversion.helpers.DataElementFinder;
import gov.cms.mat.patients.conversion.conversion.helpers.FhirCreator;
import gov.cms.mat.patients.conversion.conversion.results.QdmToFhirPatientResult;
import gov.cms.mat.patients.conversion.dao.BonniePatient;
import gov.cms.mat.patients.conversion.dao.QdmCodeSystem;
import gov.cms.mat.patients.conversion.dao.QdmDataElement;
import gov.cms.mat.patients.conversion.data.ConversionOutcome;
import gov.cms.mat.patients.conversion.exceptions.PatientConversionException;
import gov.cms.mat.patients.conversion.service.ValidationService;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.StringType;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Component
public class PatientConverter implements DataElementFinder, FhirCreator {
    public static final String US_CORE_RACE_URL = "http://hl7.org/fhir/us/core/StructureDefinition/us-core-race";
    public static final String DETAILED_RACE_URL = "http://hl7.org/fhir/us/core/ValueSet/detailed-race";


    private final ValidationService validationService;


    public PatientConverter(ValidationService validationService) {

        this.validationService = validationService;
    }

    public QdmToFhirPatientResult convert(BonniePatient bonniePatient) {
        List<String> conversionMessages = new ArrayList<>();
        Patient patient = process(bonniePatient, conversionMessages);

        ValidationResult validationResult = validationService.validate(patient);

        ConversionOutcome outcome = ConversionOutcome.builder()
                .conversionMessages(conversionMessages)
                .validationMessages(validationResult.getMessages())
                .build();

        return QdmToFhirPatientResult.builder()
                .outcome(outcome)
                .fhirPatient(patient)
                .build();
    }

    public Patient process(BonniePatient bonniePatient, List<String> conversionMessages) {
        Patient fhirPatient = new Patient();
        fhirPatient.setId(bonniePatient.get_id());
        fhirPatient.setExtension(List.of(new Extension(US_CORE_RACE_URL), new Extension(DETAILED_RACE_URL)));
        fhirPatient.setActive(true); // ??

        //  bonniePatient.getNotes(); // ??
        fhirPatient.setName(List.of(createName(bonniePatient)));

        // ?? "_type": "QDM::PatientCharacteristicBirthdate", 2 birhtdatas which one wins
        fhirPatient.setBirthDate(bonniePatient.getQdmPatient().getBirthDatetime());

        fhirPatient.setGender(processSex(bonniePatient));
        processRace(bonniePatient, fhirPatient);

        processEthnicity(bonniePatient, fhirPatient);
        fhirPatient.setDeceased(processExpired(bonniePatient));

        return fhirPatient;
    }

    private DateTimeType processExpired(BonniePatient bonniePatient) {
        var optional = findOptionalDataElementsByType(bonniePatient, "QDM::PatientCharacteristicExpired");

        if (optional.isPresent()) {
            QdmDataElement dataElement = optional.get();
            log.debug("Patient is dead");
            return new DateTimeType(dataElement.getExpiredDatetime());
        } else {
            log.debug("Patient is alive");
            return null;
        }
    }

    private HumanName createName(BonniePatient bonniePatient) {
        HumanName humanName = new HumanName();
        humanName.setUse(HumanName.NameUse.USUAL); // ??

        humanName.setFamily(bonniePatient.getFamilyName());

        if (!CollectionUtils.isEmpty(bonniePatient.getGivenNames())) {
            List<StringType> fhirNames = bonniePatient.getGivenNames().stream()
                    .map(StringType::new)
                    .collect(Collectors.toList());

            humanName.setGiven(fhirNames);
        }

        return humanName;
    }

    private void processRace(BonniePatient bonniePatient, Patient fhirPatient) {
        try {
            QdmCodeSystem qdmCodeSystem = findOneCodeSystemWithRequiredDisplay(bonniePatient, "QDM::PatientCharacteristicRace");
            Extension extension = fhirPatient.getExtensionByUrl(US_CORE_RACE_URL);

            extension.setValue(new CodeType(qdmCodeSystem.getCode()));
        } catch (PatientConversionException e) {
            log.info(e.getMessage());
        }
    }

    private void processEthnicity(BonniePatient bonniePatient, Patient fhirPatient) {
        try {
            QdmCodeSystem qdmCodeSystem = findOneCodeSystemWithRequiredDisplay(bonniePatient, "QDM::PatientCharacteristicEthnicity");
            Extension extension = fhirPatient.getExtensionByUrl(DETAILED_RACE_URL);

            extension.setValue(new CodeType(qdmCodeSystem.getCode()));
        } catch (PatientConversionException e) {
            log.info(e.getMessage());
        }
    }

    private Enumerations.AdministrativeGender processSex(BonniePatient bonniePatient) {
        try {
            QdmCodeSystem qdmCodeSystem = findOneCodeSystemWithRequiredDisplay(bonniePatient, "QDM::PatientCharacteristicSex");

            Enumerations.AdministrativeGender value = Enumerations.AdministrativeGender.UNKNOWN;
            if (qdmCodeSystem.getDisplay().toLowerCase().startsWith("m")) { // sometimes Male and M
                value = Enumerations.AdministrativeGender.MALE;
            }

            if (qdmCodeSystem.getDisplay().toLowerCase().startsWith("f")) {
                value = Enumerations.AdministrativeGender.FEMALE;
            }

            return value;
        } catch (PatientConversionException e) {
            log.info(e.getMessage());
            return null;
        }
    }
}
