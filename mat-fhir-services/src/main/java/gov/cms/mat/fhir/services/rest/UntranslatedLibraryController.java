package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.services.components.library.UnConvertedCqlLibraryHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/untranslated/library")
@Tag(name = "Untranslated_Library-Controller", description = "API for for getting conversion results.")
@Slf4j
public class UntranslatedLibraryController {
    private final UnConvertedCqlLibraryHandler unConvertedCqlLibraryHandler;

    public UntranslatedLibraryController(UnConvertedCqlLibraryHandler unConvertedCqlLibraryHandler) {
        this.unConvertedCqlLibraryHandler = unConvertedCqlLibraryHandler;
    }

    @Operation(summary = "Display all untranslated libraries",
            description = "Display the untranslated libraries that could not be located in HAPI FHIR.")
    @GetMapping()
    public List<String> findMissingValueSets() {
        return unConvertedCqlLibraryHandler.findAll();
    }

    @Operation(summary = "Display all the cql in file",
            description = "Display the untranslated libraries cql.")
    @GetMapping("/{name}")
    public String findMissingValueSets(@PathVariable String name) {
        return unConvertedCqlLibraryHandler.findCql(name);
    }
}
