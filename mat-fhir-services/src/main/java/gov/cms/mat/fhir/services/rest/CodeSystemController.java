package gov.cms.mat.fhir.services.rest;


import gov.cms.mat.fhir.services.service.CodeSystemConversionDataService;
import gov.cms.mat.fhir.services.service.FhirLoaderService;
import gov.cms.mat.fhir.services.summary.ConversionData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/codeSystem")
@Tag(name = "Code System-Controller", description = "API for loading code systems")
@Slf4j
public class CodeSystemController {

    private final CodeSystemConversionDataService codeSystemService;

    public CodeSystemController(CodeSystemConversionDataService codeSystemService) {
        this.codeSystemService = codeSystemService;
    }

    @Operation(summary = "getCodesystemMappings",
            description = "Returns the codesystem oid <-> url mappings.")
    @GetMapping("/getCodesystemMappings")
    public ConversionData getCodesystemMappings() {
        return codeSystemService.reload();
    }
}
