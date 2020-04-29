package gov.cms.mat.fhir.services.rest;


import gov.cms.mat.fhir.services.service.CodeSystemConversionDataService;
import gov.cms.mat.fhir.services.summary.CodeSystemEntry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/codeSystem")
@Tag(name = "Code System-Controller", description = "APIs for Code Systems")
@Slf4j
public class CodeSystemController {

    private final CodeSystemConversionDataService codeSystemService;

    public CodeSystemController(CodeSystemConversionDataService codeSystemService) {
        this.codeSystemService = codeSystemService;
    }

    @Operation(summary = "mappings",
            description = "Returns the clist of CodeSystemEntries from the spreadsheet.")
    @GetMapping("/mappings")
    public List<CodeSystemEntry> getCodeSystemMappings() {
        return codeSystemService.reload();
    }
}
