package gov.cms.mat.patients.conversion.conversion;

import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.patients.conversion.conversion.results.QdmToFhirConversionResult;
import gov.cms.mat.patients.conversion.dao.QdmDataElement;
import gov.cms.mat.patients.conversion.dao.QdmInterval;
import gov.cms.mat.patients.conversion.service.CodeSystemEntriesService;
import gov.cms.mat.patients.conversion.service.ValidationService;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class ParticipationConverter extends ConverterBase<Coverage> {

    public static final String QDM_TYPE = "QDM::Participation";

    public ParticipationConverter(CodeSystemEntriesService codeSystemEntriesService,
                                             FhirContext fhirContext,
                                             ObjectMapper objectMapper,
                                             ValidationService validationService) {
        super(codeSystemEntriesService, fhirContext, objectMapper, validationService);
    }

    @Override
    public String getQdmType() {
        return QDM_TYPE;
    }

    @Override
    public QdmToFhirConversionResult<Coverage> convertToFhir(Patient fhirPatient, QdmDataElement qdmDataElement) {
        Coverage coverage = new Coverage();
        List<String> conversionMessages = new ArrayList<>();

        coverage.setId(qdmDataElement.get_id());
        coverage.setBeneficiary(createReference(fhirPatient));
        coverage.setStatus(Coverage.CoverageStatus.ACTIVE);
        coverage.setType(convertToCodeSystems(getCodeSystemEntriesService(), qdmDataElement.getDataElementCodes()));
        convertParticipationPeriod(coverage, qdmDataElement);

        return QdmToFhirConversionResult.<Coverage>builder()
                .fhirResource(coverage)
                .conversionMessages(conversionMessages)
                .build();
    }

    private void convertParticipationPeriod(Coverage coverage, QdmDataElement qdmDataElement) {
        QdmInterval qdmInterval = qdmDataElement.getParticipationPeriod();
        coverage.getPeriod().setStart(qdmInterval.getLow());
        coverage.getPeriod().setEnd(qdmInterval.getHigh());
    }

}
