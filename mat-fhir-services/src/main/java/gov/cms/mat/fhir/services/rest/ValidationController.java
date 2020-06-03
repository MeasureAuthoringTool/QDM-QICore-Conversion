package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.commons.model.CqlLibraryExport;
import gov.cms.mat.fhir.commons.model.MeasureExport;
import gov.cms.mat.fhir.services.repository.CqlLibraryExportRepository;
import gov.cms.mat.fhir.services.repository.MeasureExportRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mat.shared.CQLError;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(path = "/validate")
@Tag(name = "ValidationController", description = "API for validating cql")
@Slf4j
@Controller
public class ValidationController {
    @Data
    @NoArgsConstructor
    @Nullable
    public static class ValidationRequest {
        private boolean validateValueSets = true;
        private boolean validateCodeSystems = true;
        private boolean validateSyntax = true;
        private boolean validateCqlToElm = true;
    }

    @Data
    @NoArgsConstructor
    @Nullable
    public static class ValidationResponse {
        /**
         * This is a list of all library errors.
         * All cql-to-elm warnings are dropped.
         * CRITICAL is used when there is a syntax error coming from antlr.
         */
        List<CQLError> matErrors;
    }

    @Data
    @NoArgsConstructor
    @Nullable
    public static class LibraryErrors {
        @NotBlank
        private String id;
        @NotBlank
        private String name;
        @NotBlank
        private String version;
        List<CQLError> errors = new ArrayList<>();
    }

    private MeasureExportRepository measureExportRepo;
    private CqlLibraryExportRepository cqlLibExportRepo;

    public ValidationController(MeasureExportRepository measureExportRepo,
                                CqlLibraryExportRepository cqlLibExportRepo) {
        this.measureExportRepo = measureExportRepo;
        this.cqlLibExportRepo = cqlLibExportRepo;
    }

    @GetMapping("/standalone-lib/{id}")
    public @ResponseBody List<CQLError> validateStandaloneLib(@NotBlank @RequestHeader(value = "ULMS-TOKEN") String ulmsToken,
                                                @NotBlank @PathVariable("id") String libId,
                                                @Valid @RequestParam ValidationRequest validationRequest) {
        try {
            CqlLibraryExport cqlLibExport = cqlLibExportRepo.getCqlLibraryExportByCqlLibraryId(libId);

            if (cqlLibExport != null) {
               byte[] cqlBytes = cqlLibExport.getCql();
                if (cqlBytes == null) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "CQL_LIBRARY_EXPORT.CQL does not exist for CQL_LIBRARY.id " + libId + "."
                    );
                }
                return validateCql(ulmsToken,
                        decode(cqlBytes),
                        validationRequest);
            } else {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "CQL_LIBRARY not found for CQL_LIBRARY.id " + libId + "."
                );
            }
        } catch (RuntimeException e) {
            log.error("fromStandaloneLib", e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unexpected error in validateStandaloneLib(" + ulmsToken + "," + libId + "," + validationRequest,
                    e);
        }
    }

    @GetMapping("/measure/{id}")
    public @ResponseBody List<CQLError> validateMeasureId(@NotBlank @RequestHeader(value = "ULMS-TOKEN") String ulmsToken,
                                     @NotBlank @PathVariable("id") String measureId,
                                            @Valid @RequestParam ValidationRequest validationRequest) {

        try {
            MeasureExport measureExport = measureExportRepo.getMeasureExportById(measureId);

            if (measureExport != null) {
                byte[] cqlBytes = measureExport.getCql();
                if (cqlBytes == null) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "MEASURE_EXPORT.CQL does not exist for MEASURE.ID " + measureId + "."
                    );
                }
                return validateCql(ulmsToken,
                        decode(cqlBytes),
                        validationRequest);
            } else {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "MEASURE_EXPORT does not exist for MEASURE.ID " + measureId + "."
                );
            }
        } catch (RuntimeException e) {
            log.error("fromStandaloneLib", e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unexpected error in validateMeasureId(" + ulmsToken + "," + measureId + "," + validationRequest,
                    e);
        }
    }

    @PostMapping("/cql")
    public @ResponseBody List<CQLError> validateCql(@NotBlank @RequestHeader(value = "ULMS-TOKEN") String ulmsToken,
                                      @NotBlank @RequestParam String cql,
                                      @Valid @RequestParam ValidationRequest validationRequest) {
        List<CQLError> result = new ArrayList<>();
        // Run in parallel:
        //    1) Validate with cql-to-elm translator
        //    2) Validate with antlr/additional validations.
        return result;
    }

    private String decode(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
