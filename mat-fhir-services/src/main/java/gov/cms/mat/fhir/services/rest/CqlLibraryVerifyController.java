package gov.cms.mat.fhir.services.rest;


import gov.cms.mat.fhir.services.service.CQLLibraryTranslationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/cql")
@Tag(name = "CQL-Library-Controller", description = "API for verifying cql library conversion")
@Slf4j
public class CqlLibraryVerifyController {

    private final CQLLibraryTranslationService cqlLibraryTranslationService;

    public CqlLibraryVerifyController(CQLLibraryTranslationService cqlLibraryTranslationService) {
        this.cqlLibraryTranslationService = cqlLibraryTranslationService;
    }

    @Operation(summary = "Translate all ValueSets in MAT to FHIR.",
            description = "Translate all the ValueSets in the MAT Database and persist to the HAPI FHIR Database.")
    @Transactional(readOnly = true)
    @PutMapping(path = "/translateAll")
    public String translateAll() {
        return cqlLibraryTranslationService.processAll();
    }

    @Operation(summary = "Translate all ValueSets in MAT to FHIR.",
            description = "Translate all the ValueSets in the MAT Database and persist to the HAPI FHIR Database.")
    @Transactional(readOnly = true)
    @PutMapping(path = "/translateOne")
    public String translateOne(@RequestParam String measureId) {
        return cqlLibraryTranslationService.processOne(measureId);
    }

}


