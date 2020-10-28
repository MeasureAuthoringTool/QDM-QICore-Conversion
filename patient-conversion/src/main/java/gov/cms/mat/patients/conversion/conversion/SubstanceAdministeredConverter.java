package gov.cms.mat.patients.conversion.conversion;


import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.patients.conversion.conversion.results.QdmToFhirConversionResult;
import gov.cms.mat.patients.conversion.dao.QdmDataElement;
import gov.cms.mat.patients.conversion.service.CodeSystemEntriesService;
import gov.cms.mat.patients.conversion.service.ValidationService;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.NutritionOrder;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class SubstanceAdministeredConverter extends ConverterBase<NutritionOrder> {
    public static final String QDM_TYPE = "QDM::SubstanceAdministered";

    public SubstanceAdministeredConverter(CodeSystemEntriesService codeSystemEntriesService,
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
    public QdmToFhirConversionResult<NutritionOrder> convertToFhir(Patient fhirPatient, QdmDataElement qdmDataElement) {

        List<String> conversionMessages = new ArrayList<>();

        NutritionOrder nutritionOrder = new NutritionOrder();
        //http://hl7.org/fhir/us/qicore/qdm-to-qicore.html#821-substance
        nutritionOrder.setStatus(NutritionOrder.NutritionOrderStatus.UNKNOWN); // Constrain to Active, on-hold, Completed
        conversionMessages.add(NO_STATUS_MAPPING);

        nutritionOrder.setIntent(NutritionOrder.NutritiionOrderIntent.NULL); // todo NO intent for SubstanceAdministered todo find

        nutritionOrder.setId(qdmDataElement.get_id());
        nutritionOrder.setPatient(createReference(fhirPatient));
        nutritionOrder.setDateTime(qdmDataElement.getAuthorDatetime());

        nutritionOrder.getOralDiet().addType(convertToCodeSystems(codeSystemEntriesService,  qdmDataElement.getDataElementCodes()));

        if( qdmDataElement.getRelevantPeriod() != null) {
          conversionMessages.add("Unable to convert RelevantPeriod to a Fhir Timing object");
        }


        return QdmToFhirConversionResult.<NutritionOrder>builder()
                .fhirResource(nutritionOrder)
                .conversionMessages(conversionMessages)
                .build();

    }


}
