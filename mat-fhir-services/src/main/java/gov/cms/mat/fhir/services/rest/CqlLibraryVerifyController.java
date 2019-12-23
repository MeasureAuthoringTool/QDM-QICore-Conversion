package gov.cms.mat.fhir.services.rest;


import gov.cms.mat.fhir.rest.cql.ConversionType;
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
@Tag(name = "CQL-Library-Controller", description = "API for converting and verifying cql library conversion")
@Slf4j
public class CqlLibraryVerifyController {
    private final CQLLibraryTranslationService cqlLibraryTranslationService;

    public CqlLibraryVerifyController(CQLLibraryTranslationService cqlLibraryTranslationService) {
        this.cqlLibraryTranslationService = cqlLibraryTranslationService;
    }

    @Operation(summary = "Translate all Mat XMl cql libraries generating report",
            description = "Translate all Mat XMl cql libraries generating report in the mongo DB")
    @Transactional(readOnly = true)
    @PutMapping(path = "/translateAll")
    public String translateAll() {
        return cqlLibraryTranslationService.processAll();
    }

    @Operation(summary = "Translate one CqlLibrary in MAT to FHIR.",
            description = "Translate one CqlLibrary in MAT to FHIR identified by the measureId and return the library in json")
    @Transactional(readOnly = true)
    @PutMapping(path = "/translateOne")
    public String translateOne(@RequestParam String measureId) {
        return cqlLibraryTranslationService.processOne(measureId, ConversionType.CONVERSION);
    }
}


