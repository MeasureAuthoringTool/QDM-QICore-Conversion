package gov.cms.mat.patients.conversion.conversion.helpers;


import gov.cms.mat.fhir.rest.dto.spreadsheet.CodeSystemEntry;
import gov.cms.mat.patients.conversion.dao.BonniePatient;
import gov.cms.mat.patients.conversion.dao.DataElements;
import gov.cms.mat.patients.conversion.dao.QdmCodeSystem;
import gov.cms.mat.patients.conversion.dao.QdmPatient;
import gov.cms.mat.patients.conversion.exceptions.PatientConversionException;
import gov.cms.mat.patients.conversion.service.CodeSystemEntriesService;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public interface DataElementFinder {

    default CodeableConcept convertToCodeSystems(CodeSystemEntriesService codeSystemEntriesService,
                                                 List<QdmCodeSystem> dataElementCodes) {
        if (dataElementCodes.isEmpty()) {
            return null;
        } else {
            CodeableConcept codeableConcept = new CodeableConcept();

            List<Coding> codings = dataElementCodes.stream()
                    .map(c -> convertToCoding(codeSystemEntriesService, c))
                    .collect(Collectors.toList());

            return codeableConcept.setCoding(codings);
        }
    }

    default CodeableConcept convertToCodeableConcept(CodeSystemEntriesService codeSystemEntriesService, QdmCodeSystem qdmCodeSystem) {
        return new CodeableConcept().setCoding(List.of(convertToCoding(codeSystemEntriesService, qdmCodeSystem)));
    }

    default Coding convertToCoding(CodeSystemEntriesService codeSystemEntriesService, QdmCodeSystem qdmCodeSystem) {
        CodeSystemEntry codeSystemEntry = codeSystemEntriesService.findRequired(qdmCodeSystem.getSystem());

        return new Coding(codeSystemEntry.getUrl(), qdmCodeSystem.getCode(), qdmCodeSystem.getDisplay());
    }


    default Coding createCodingFromDataElementCodes(CodeSystemEntriesService codeSystemEntriesService,
                                                    List<QdmCodeSystem> dataElementCodes) {
        if (CollectionUtils.isEmpty(dataElementCodes)) {
            return null;
        } else {
            QdmCodeSystem qdmCodeSystem = dataElementCodes.get(0);// if many will take 1st one

            var optional = codeSystemEntriesService.find(qdmCodeSystem.getSystem());

            if (optional.isEmpty()) {
                return new Coding(qdmCodeSystem.getSystem(), qdmCodeSystem.getCode(), qdmCodeSystem.getDisplay());
            } else {
                CodeSystemEntry codeSystemEntry = optional.get();
                return new Coding(codeSystemEntry.getUrl(), qdmCodeSystem.getCode(), qdmCodeSystem.getDisplay());
            }
        }
    }


    default Optional<DataElements> findOptionalDataElementsByType(BonniePatient bonniePatient, String type) {
        List<DataElements> dataElements = findDataElementsByType(bonniePatient, type);

        if (dataElements.isEmpty()) {
            return Optional.empty();
        } else {
            if (dataElements.size() > 1) {
                throw new PatientConversionException(String.format("Patient %s has %s %s elements. Should be one.",
                        bonniePatient.identifier(), dataElements.size(), type));
            } else {
                return Optional.of(dataElements.get(0));
            }
        }
    }

    default DataElements findOneDataElementsByType(BonniePatient bonniePatient, String type) {
        List<DataElements> dataElements = findDataElementsByType(bonniePatient, type);

        if (dataElements.isEmpty()) {
            throw new PatientConversionException(String.format("Patient %s has no %s",
                    type, bonniePatient.identifier()));
        } else {
            if (dataElements.size() > 1) {
                throw new PatientConversionException(String.format("Patient %s has %s %s elements. Should be one.",
                        bonniePatient.identifier(), dataElements.size(), type));
            } else {
                return dataElements.get(0);
            }
        }
    }

    default List<DataElements> findDataElementsByType(BonniePatient bonniePatient, String type) {
        QdmPatient qdmPatient = bonniePatient.getQdmPatient();

        if (CollectionUtils.isEmpty(qdmPatient.getDataElements())) {
            return Collections.emptyList();
        } else {
            return qdmPatient.getDataElements().stream()
                    .filter(d -> d.getType().equals(type))
                    .collect(Collectors.toList());
        }
    }

    default QdmCodeSystem findOneCodeSystemWithRequiredDisplay(BonniePatient bonniePatient, String type) {
        DataElements element = findOneDataElementsByType(bonniePatient, type);

        QdmCodeSystem qdmCodeSystem = getOneCodeSystem(element);

        if (qdmCodeSystem.getDisplay() == null) {
            throw new PatientConversionException(String.format("DataElement %s has no dataElementCodes. codeSystem: %s",
                    element.identifier(), qdmCodeSystem));
        }

        return qdmCodeSystem;
    }

    default QdmCodeSystem getOneCodeSystem(DataElements element) {
        List<QdmCodeSystem> dataElementCodes = element.getDataElementCodes();

        if (dataElementCodes.isEmpty()) {
            throw new PatientConversionException(String.format("DataElement %s has no dataElementCodes",
                    element.identifier()));
        } else {
            if (dataElementCodes.size() > 1) {
                throw new PatientConversionException(String.format("DataElement %s has %s dataElementCodes. Should be one.",
                        element.identifier(), dataElementCodes.size()));
            } else {
                return dataElementCodes.get(0);
            }
        }
    }
}
