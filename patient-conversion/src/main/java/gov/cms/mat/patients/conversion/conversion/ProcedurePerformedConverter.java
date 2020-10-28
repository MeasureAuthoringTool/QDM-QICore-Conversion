package gov.cms.mat.patients.conversion.conversion;

import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.patients.conversion.conversion.helpers.ProcedureConverter;
import gov.cms.mat.patients.conversion.conversion.results.QdmToFhirConversionResult;
import gov.cms.mat.patients.conversion.dao.QdmDataElement;
import gov.cms.mat.patients.conversion.service.CodeSystemEntriesService;
import gov.cms.mat.patients.conversion.service.ValidationService;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Procedure;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProcedurePerformedConverter extends ConverterBase<Procedure> implements ProcedureConverter {

    public static final String QDM_TYPE = "QDM::ProcedurePerformed";

    public ProcedurePerformedConverter(CodeSystemEntriesService codeSystemEntriesService,
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
    public QdmToFhirConversionResult<Procedure> convertToFhir(Patient fhirPatient, QdmDataElement qdmDataElement) {
        return convertToFhirProcedure(fhirPatient, qdmDataElement, this);
    }

    @Override
    void convertNegation(QdmDataElement qdmDataElement, Procedure procedure) {
        convertNegationProcedure(qdmDataElement, procedure);
    }
}
