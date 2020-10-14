package gov.cms.mat.patients.conversion.service;

import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.patients.conversion.conversion.ConverterBase;
import gov.cms.mat.patients.conversion.conversion.EncounterConverter;
import gov.cms.mat.patients.conversion.conversion.InterventionOrderConverter;
import gov.cms.mat.patients.conversion.conversion.InterventionPerformedConverter;
import gov.cms.mat.patients.conversion.conversion.MedicationDischargeConverter;
import gov.cms.mat.patients.conversion.conversion.PatientConverter;
import gov.cms.mat.patients.conversion.conversion.helpers.FhirCreator;
import gov.cms.mat.patients.conversion.dao.BonniePatient;
import gov.cms.mat.patients.conversion.dao.QdmDataElement;
import gov.cms.mat.patients.conversion.data.ConversionResult;
import gov.cms.mat.patients.conversion.data.FhirDataElement;
import gov.cms.mat.patients.conversion.exceptions.PatientConversionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PatientService implements FhirCreator {
    private static final int THREAD_POOL_TIMEOUT_MINUTES = 1;
    private final PatientConverter patientConverter;
    private final EncounterConverter encounterConverter;
    private final InterventionOrderConverter interventionOrderConverter;
    private final InterventionPerformedConverter interventionPerformedConverter;
    private final MedicationDischargeConverter medicationDischargeConverter;
    private final ObjectMapper objectMapper;

    private final FhirContext fhirContext;

    public PatientService(PatientConverter patientConverter,
                          EncounterConverter encounterConverter,
                          InterventionOrderConverter interventionOrderConverter,
                          InterventionPerformedConverter interventionPerformedConverter,
                          MedicationDischargeConverter medicationDischargeConverter,
                          FhirContext fhirContext,
                          ObjectMapper objectMapper) {
        this.patientConverter = patientConverter;
        this.encounterConverter = encounterConverter;
        this.interventionOrderConverter = interventionOrderConverter;
        this.interventionPerformedConverter = interventionPerformedConverter;
        this.medicationDischargeConverter = medicationDischargeConverter;
        this.fhirContext = fhirContext;
        this.objectMapper = objectMapper;
    }

    public List<ConversionResult> processMany(List<BonniePatient> bonniePatients) {
        List<ConversionResult> results = bonniePatients.parallelStream()
                .map(this::processOne)
                .collect(Collectors.toList());

        //  maintain the order from the input list
        return bonniePatients.stream()
                .map(b -> findResultById(b.get_id(), results))
                .collect(Collectors.toList());
    }

    public ConversionResult processOne(BonniePatient bonniePatient) {
        try {
            List<CompletableFuture<List<FhirDataElement>>> futures = new ArrayList<>();

            var qdmTypes = collectQdmTypes(bonniePatient);

            Patient fhirPatient = patientConverter.process(bonniePatient);

            if (qdmTypes.contains(EncounterConverter.QDM_TYPE)) {
                processFuture(bonniePatient, fhirPatient, encounterConverter, futures);
            }

            if (qdmTypes.contains(InterventionOrderConverter.QDM_TYPE)) {
                processFuture(bonniePatient, fhirPatient, interventionOrderConverter, futures);
            }

            if (qdmTypes.contains(InterventionPerformedConverter.QDM_TYPE)) {
                processFuture(bonniePatient, fhirPatient, interventionPerformedConverter, futures);
            }

            if (qdmTypes.contains(MedicationDischargeConverter.QDM_TYPE)) {
                processFuture(bonniePatient, fhirPatient, medicationDischargeConverter, futures);
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();

            List<FhirDataElement> fhirDataElements = findFhirDataElementsFromFutures(futures);

            Instant timeStamp = Instant.now();
            return ConversionResult.builder()
                    .id(bonniePatient.get_id())
                    .expectedValues(bonniePatient.getExpectedValues())
                    .measureIds(bonniePatient.getMeasureIds())
                    .fhirPatient(objectMapper.readTree(toJson(fhirContext, fhirPatient)))
                    .dataElements(fhirDataElements)
                    .createdAt(timeStamp)
                    .updatedAt(timeStamp)
                    .build();

        } catch (Exception e) {
            log.warn("Error ", e);
            throw new PatientConversionException("Error");
        }
    }

    private List<FhirDataElement> findFhirDataElementsFromFutures(List<CompletableFuture<List<FhirDataElement>>> futures) {
        return futures.stream()
                .map(this::findDataFromFuture)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private List<FhirDataElement> findDataFromFuture(CompletableFuture<List<FhirDataElement>> f) {
        try {
            return f.get();
        } catch (Exception e) {
            log.error("Error with future get", e);
            return Collections.emptyList();
        }
    }

    public Set<String> collectQdmTypes(BonniePatient bonniePatient) {

        if (bonniePatient.getQdmPatient() == null || CollectionUtils.isEmpty(bonniePatient.getQdmPatient().getDataElements())) {
            log.warn("Bonnie Patient id: {} has no data elements", bonniePatient.get_id());
            return Collections.emptySet();
        } else {
            return bonniePatient.getQdmPatient().getDataElements().stream()
                    .map(QdmDataElement::get_type)
                    .collect(Collectors.toSet());
        }
    }

    private void processFuture(BonniePatient bonniePatient,
                               Patient fhirPatient,
                               ConverterBase<? extends IBaseResource> converter,
                               List<CompletableFuture<List<FhirDataElement>>> futures) {
        CompletableFuture<List<FhirDataElement>> future = converter.convertToString(bonniePatient, fhirPatient);
        future.orTimeout(THREAD_POOL_TIMEOUT_MINUTES, TimeUnit.MINUTES);
        futures.add(future);
    }

    private ConversionResult findResultById(String id, List<ConversionResult> results) {
        return results.stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No result found with the id: " + id));
    }
}
