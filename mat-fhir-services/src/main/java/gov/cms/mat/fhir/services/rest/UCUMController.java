package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.services.service.UCUMValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping(path = "/ucum")
@Tag(name = "UCUM-Controller",
        description = "API for validating ucum codes")
@Slf4j
@RequiredArgsConstructor
public class UCUMController {
    private final UCUMValidationService ucumValidationService;

    @Operation(summary = "Validate",
            description = "Validate the ucum unit")
    @GetMapping("/{unit}")
    public Boolean validateCode(@PathVariable("unit") String unit) {
        return ucumValidationService.validate(URLDecoder.decode(unit, StandardCharsets.UTF_8));
    }
}
