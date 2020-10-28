package gov.cms.mat.fhir.services.cql.parser;

import gov.cms.mat.fhir.rest.dto.spreadsheet.CodeSystemEntry;
import gov.cms.mat.fhir.services.service.MappingDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class ManageCodeListServiceImpl implements CodeListService {

    private static final String OID_TO_VSAC_CODE_SYSTEM_DTO = "oidToVSACDodeSystemDTO";

    private final MappingDataService mappingDataService;

    public ManageCodeListServiceImpl(MappingDataService mappingDataService) {

        this.mappingDataService = mappingDataService;
    }

    @Cacheable(OID_TO_VSAC_CODE_SYSTEM_DTO)
    @Override
    public Map<String, CodeSystemEntry> getOidToVsacCodeSystemMap() {
        Map<String, CodeSystemEntry> result = new HashMap<>();
        mappingDataService.getCodeSystemEntries().forEach(v -> result.put(v.getOid(), v));
        return result;
    }
}