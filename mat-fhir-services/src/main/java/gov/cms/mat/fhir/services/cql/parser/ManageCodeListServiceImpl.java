package gov.cms.mat.fhir.services.cql.parser;

import java.util.HashMap;
import java.util.Map;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import gov.cms.mat.fhir.services.service.CodeSystemConversionDataService;
import gov.cms.mat.fhir.services.summary.CodeSystemEntry;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ManageCodeListServiceImpl implements CodeListService {

    private static final String OID_TO_VSAC_CODE_SYSTEM_DTO = "oidToVSACDodeSystemDTO";

    private final CodeSystemConversionDataService codeSystemService;

    public ManageCodeListServiceImpl(final CodeSystemConversionDataService codeSystemService) {
        this.codeSystemService = codeSystemService;
    }

    @Cacheable(OID_TO_VSAC_CODE_SYSTEM_DTO)
    @Override
    public Map<String, CodeSystemEntry> getOidToVsacCodeSystemMap() {
        Map<String, CodeSystemEntry> result = new HashMap<>();

        codeSystemService.reload().forEach(v -> result.put(v.getOid(), v));
        log.info("Exiting getCodesSystemMappings " + result);
        return result;
    }
    
}