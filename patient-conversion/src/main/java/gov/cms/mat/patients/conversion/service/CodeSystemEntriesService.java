package gov.cms.mat.patients.conversion.service;

import gov.cms.mat.fhir.rest.dto.spreadsheet.CodeSystemEntry;
import gov.cms.mat.patients.conversion.exceptions.PatientConversionException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CodeSystemEntriesService {
    private final MappingDataService mappingDataService;

    public CodeSystemEntriesService(MappingDataService mappingDataService) {
        this.mappingDataService = mappingDataService;
    }

    public Optional<CodeSystemEntry> find(String oid) {
        return mappingDataService.getCodeSystemEntries().stream()
                .filter(c -> c.getOid().contains(oid))
                .findFirst();
    }

    public CodeSystemEntry findRequired(String oid) {
        var optional = find(oid);

        if (optional.isEmpty()) {
            throw new PatientConversionException("Cannot find CodeSystemEntry with oid: " + oid);
        }

        return optional.get();
    }
}
