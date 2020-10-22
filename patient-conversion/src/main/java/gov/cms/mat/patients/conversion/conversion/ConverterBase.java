package gov.cms.mat.patients.conversion.conversion;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.ValidationResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.patients.conversion.conversion.helpers.DataElementFinder;
import gov.cms.mat.patients.conversion.conversion.helpers.FhirCreator;
import gov.cms.mat.patients.conversion.conversion.results.QdmToFhirConversionResult;
import gov.cms.mat.patients.conversion.dao.BonniePatient;
import gov.cms.mat.patients.conversion.dao.QdmDataElement;
import gov.cms.mat.patients.conversion.data.ConversionOutcome;
import gov.cms.mat.patients.conversion.data.FhirDataElement;
import gov.cms.mat.patients.conversion.exceptions.MappingServiceException;
import gov.cms.mat.patients.conversion.service.CodeSystemEntriesService;
import gov.cms.mat.patients.conversion.service.ValidationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Procedure;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.springframework.scheduling.annotation.Async;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
public abstract class ConverterBase<T extends IBaseResource> implements FhirCreator, DataElementFinder {
    static final String QICORE_NOT_DONE = "http://hl7.org/fhir/us/qicore/StructureDefinition/qicore-notDone";
    static final String  QICORE_DO_NOT_PERFORM_REASON = "http://hl7.org/fhir/us/qicore/StructureDefinition/qicore-doNotPerformReason";
    private static final String QICORE_RECORDED = "http://hl7.org/fhir/us/qicore/StructureDefinition/qicore-recorded";

    static final String NO_STATUS_MAPPING = "No mapping for status";

    final CodeSystemEntriesService codeSystemEntriesService;
    final FhirContext fhirContext;
    final ObjectMapper objectMapper;
    final ValidationService validationService;

    public ConverterBase(CodeSystemEntriesService codeSystemEntriesService,
                         FhirContext fhirContext,
                         ObjectMapper objectMapper,
                         ValidationService validationService) {
        this.codeSystemEntriesService = codeSystemEntriesService;
        this.fhirContext = fhirContext;
        this.objectMapper = objectMapper;
        this.validationService = validationService;
    }

    @Async("threadPoolConversion")
    public CompletableFuture<List<FhirDataElement>> convertToFhirDataElement(BonniePatient bonniePatient, Patient fhirPatient) {
        List<FhirDataElement> encounters = createDataElements(bonniePatient, fhirPatient);
        return CompletableFuture.completedFuture(encounters);
    }

    public List<FhirDataElement> createDataElements(BonniePatient bonniePatient, Patient fhirPatient) {
        List<QdmDataElement> dataElements = findDataElementsByType(bonniePatient, getQdmType());

        if (dataElements.isEmpty()) {
            return Collections.emptyList();
        } else {
            return dataElements.stream()
                    .map(d -> convertQdmToFhir(fhirPatient, d))
                    .collect(Collectors.toList());
        }
    }

    FhirDataElement buildDataElement(QdmToFhirConversionResult qdmToFhirConversionResult,
                                     ValidationResult validationResult,
                                     QdmDataElement dataElement) {
        try {

            ConversionOutcome conversionOutcome = ConversionOutcome.builder()
                    .conversionMessages(qdmToFhirConversionResult.getConversionMessages())
                    .validationMessages(validationResult.getMessages())
                    .build();

            String valueSetTitle = findValueSetTitle(dataElement.getDescription());

            String description = qdmToFhirConversionResult.getFhirResource().getClass().getSimpleName() + "::" + valueSetTitle;

            return FhirDataElement.builder()
                    .codeListId(dataElement.getCodeListId())
                    .description(description)
                    .valueSetTitle(valueSetTitle)
                    .fhirResource(createFhirResourceJsonNode(qdmToFhirConversionResult.getFhirResource()))
                    .outcome(conversionOutcome)
                    .build();
        } catch (JsonProcessingException e) {
            log.error("Cannot create DataElement", e);
            throw new MappingServiceException(e.getMessage());
        }
    }

    private String findValueSetTitle(String description) {
        if (StringUtils.isBlank(description)) {
            return description;
        } else {
            String[] splits = description.split(":");

            if (splits.length != 2) {
                log.warn("Cannot find valueSetTitle in description: {}", description);
                return description;
            } else {
                return splits[1].trim();
            }
        }
    }

    private JsonNode createFhirResourceJsonNode(IBaseResource fhirResource) throws JsonProcessingException {
        String json = toJson(fhirContext, fhirResource);
        return objectMapper.readTree(json);
    }

    abstract String getQdmType();

    abstract QdmToFhirConversionResult convertToFhir(Patient fhirPatient, QdmDataElement qdmDataElement);

    public FhirDataElement convertQdmToFhir(Patient fhirPatient, QdmDataElement dataElement) {
        QdmToFhirConversionResult qdmToFhirConversionResult = convertToFhir(fhirPatient, dataElement);

        ValidationResult validationResult = validationService.validate(qdmToFhirConversionResult.getFhirResource());

        return buildDataElement(qdmToFhirConversionResult, validationResult, dataElement);
    }

    boolean processNegation( QdmDataElement qdmDataElement, T resource) {
        if (qdmDataElement.getNegationRationale() != null) {
            convertNegation( qdmDataElement, resource );
            return true;
        } else {
            log.trace("No negations found");
            return false;
        }
    }

   void convertNegation(QdmDataElement qdmDataElement, T resource) {
        log.warn("Negation not handled - implement this method");

        //todo remove me
       try {
           System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(qdmDataElement));
       } catch (JsonProcessingException e) {
           e.printStackTrace();
       }
    }

    void convertNegationServiceRequest (QdmDataElement qdmDataElement,  ServiceRequest serviceRequest) {
        serviceRequest.setStatus(ServiceRequest.ServiceRequestStatus.COMPLETED);
        serviceRequest.setDoNotPerform(true);

        Extension extensionDoNotPerformReason = new Extension(QICORE_DO_NOT_PERFORM_REASON);
        extensionDoNotPerformReason.setValue(convertToCoding(codeSystemEntriesService, qdmDataElement.getNegationRationale()));
        serviceRequest.setExtension(List.of(extensionDoNotPerformReason));
    }

    void convertNegationProcedure(QdmDataElement qdmDataElement, Procedure procedure) {
        // http://hl7.org/fhir/us/qicore/Procedure-negation-example.json.html
        procedure.setStatus(Procedure.ProcedureStatus.NOTDONE);

        Extension extensionNotDone = new Extension(QICORE_NOT_DONE);
        extensionNotDone.setValue(new BooleanType(true));
        procedure.setModifierExtension(List.of(extensionNotDone));

        //todo stan is this correct
        Extension extensionNotDoneReason = new Extension(QICORE_RECORDED);
        extensionNotDoneReason.setValue(new DateTimeType(qdmDataElement.getAuthorDatetime()));
        procedure.setExtension(List.of(extensionNotDoneReason));

        procedure.setStatusReason(convertToCodeableConcept(codeSystemEntriesService, qdmDataElement.getNegationRationale()));
    }
}
