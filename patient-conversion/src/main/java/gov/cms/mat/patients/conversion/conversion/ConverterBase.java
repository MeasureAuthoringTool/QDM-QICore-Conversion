package gov.cms.mat.patients.conversion.conversion;

import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.patients.conversion.conversion.helpers.DataElementFinder;
import gov.cms.mat.patients.conversion.conversion.helpers.FhirCreator;
import gov.cms.mat.patients.conversion.dao.BonniePatient;
import gov.cms.mat.patients.conversion.dao.QdmDataElement;
import gov.cms.mat.patients.conversion.data.FhirDataElement;
import gov.cms.mat.patients.conversion.exceptions.MappingServiceException;
import gov.cms.mat.patients.conversion.service.CodeSystemEntriesService;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.scheduling.annotation.Async;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
public abstract class ConverterBase<T extends IBaseResource> implements FhirCreator, DataElementFinder {
    final CodeSystemEntriesService codeSystemEntriesService;
    final FhirContext fhirContext;
    final ObjectMapper objectMapper;

    public ConverterBase(CodeSystemEntriesService codeSystemEntriesService,
                         FhirContext fhirContext,
                         ObjectMapper objectMapper) {
        this.codeSystemEntriesService = codeSystemEntriesService;
        this.fhirContext = fhirContext;
        this.objectMapper = objectMapper;
    }

    @Async("threadPoolConversion")
    public CompletableFuture<List<FhirDataElement>> convertToString(BonniePatient bonniePatient, Patient fhirPatient) {
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

    FhirDataElement buildDataElement(IBaseResource fhirResource, QdmDataElement dataElement) {
        try {
            return FhirDataElement.builder()
                    .codeListId(dataElement.getCodeListId())
                    .description(dataElement.getQdmTitle())
                    .valueSetTitle(dataElement.get_type() )
                    .fhirResource(createFhirResourceJsonNode(fhirResource))
                    .build();
        } catch (JsonProcessingException e) {
            log.error("Cannot create DataElement", e);
            throw new MappingServiceException(e.getMessage());
        }
    }

    private JsonNode createFhirResourceJsonNode(IBaseResource fhirResource) throws JsonProcessingException {
        String json = toJson(fhirContext, fhirResource);
        return objectMapper.readTree(json);
    }

    abstract String getQdmType();

    abstract T convertToFhir(Patient fhirPatient, QdmDataElement dataElement);

    public FhirDataElement convertQdmToFhir(Patient fhirPatient, QdmDataElement dataElement) {
        T fhirResource = convertToFhir(fhirPatient, dataElement);
        return buildDataElement(fhirResource, dataElement);
    }
}
