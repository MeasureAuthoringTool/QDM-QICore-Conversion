package gov.cms.mat.patients.conversion.service;

import gov.cms.mat.fhir.rest.dto.spreadsheet.CodeSystemEntry;
import gov.cms.mat.patients.conversion.exceptions.PatientConversionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class CodeSystemEntriesService {
    private final MappingDataService mappingDataService;

    public CodeSystemEntriesService(MappingDataService mappingDataService) {
        this.mappingDataService = mappingDataService;
    }

    public Optional<CodeSystemEntry> find(String oid) {
        if (StringUtils.isBlank(oid)) {
            log.warn("Oid is empty: {}", oid);
            return Optional.empty();
        } else {
            return mappingDataService.getCodeSystemEntries().stream()
                    .filter(c -> c.getOid().contains(oid))
                    .findFirst();
        }
    }

    public CodeSystemEntry findRequired(String oid) {
        var optional = find(oid);

        if (optional.isEmpty()) {
            String errorMessage = "Cannot find CodeSystemEntry with oid: " + oid;
            log.info(errorMessage);
            throw new PatientConversionException(errorMessage);
        }

        return optional.get();
    }
}
