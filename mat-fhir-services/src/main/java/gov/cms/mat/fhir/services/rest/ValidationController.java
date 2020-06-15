package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.commons.model.CqlLibraryExport;
import gov.cms.mat.fhir.commons.model.MeasureExport;
import gov.cms.mat.fhir.services.repository.CqlLibraryExportRepository;
import gov.cms.mat.fhir.services.repository.MeasureExportRepository;
import gov.cms.mat.fhir.services.rest.dto.LibraryErrors;
import gov.cms.mat.fhir.services.service.ValidationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mat.shared.CQLError;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    public static class CqlValidationRequest {
        private String cql;
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

    private final MeasureExportRepository measureExportRepo;
    private final CqlLibraryExportRepository cqlLibExportRepo;
    private final ValidationService validationService;

    public ValidationController(MeasureExportRepository measureExportRepo,
                                CqlLibraryExportRepository cqlLibExportRepo,
                                ValidationService validationService) {
        this.measureExportRepo = measureExportRepo;
        this.cqlLibExportRepo = cqlLibExportRepo;
        this.validationService = validationService;
    }

    @GetMapping("/standalone-lib/{id}")
    public @ResponseBody
    List<LibraryErrors> validateStandaloneLib(@NotBlank @RequestHeader(value = "ULMS-TOKEN") String ulmsToken,
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

                return validateCqlBytes(ulmsToken, validationRequest, cqlBytes);

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
    public @ResponseBody
    List<LibraryErrors> validateMeasureId(@NotBlank @RequestHeader(value = "ULMS-TOKEN") String ulmsToken,
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

                return validateCqlBytes(ulmsToken, validationRequest, cqlBytes);

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
    public @ResponseBody
    List<LibraryErrors> validateCqlRequest(@NotBlank @RequestHeader(value = "ULMS-TOKEN") String ulmsToken,
                                           @RequestBody CqlValidationRequest validationRequest) {
        return validateCql(ulmsToken, validationRequest);
    }

    private List<LibraryErrors> validateCqlBytes(String ulmsToken,
                                                 ValidationRequest validationRequest,
                                                 byte[] cqlBytes) {
        CqlValidationRequest cqlValidationRequest = buildCqlValidationRequest(validationRequest, cqlBytes);

        return validateCql(ulmsToken,
                cqlValidationRequest);
    }

    private List<LibraryErrors> validateCql(String ulmsToken, CqlValidationRequest validationRequest) {

        List<CompletableFuture<List<LibraryErrors>>> futures = new ArrayList<>();

        if (validationRequest.isValidateCqlToElm()) {
            CompletableFuture<List<LibraryErrors>> f = validationService.validateCql(validationRequest.getCql());
            f.orTimeout(30, TimeUnit.SECONDS); //todo add this as config yaml parameter
            futures.add(f);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        // Run in parallel:
        //    1) Validate with cql-to-elm translator
        //    2) Validate with antlr/additional validations.

        List<LibraryErrors> libraryErrors =
                futures.stream()
                        .map(this::getFromFuture)
                        .flatMap(List::stream)
                        .collect(Collectors.toList());

        return libraryErrors;
    }

    private List<LibraryErrors> getFromFuture(CompletableFuture<List<LibraryErrors>> l) {
        try {
            return l.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private CqlValidationRequest buildCqlValidationRequest(ValidationRequest requestIn, byte[] cqlBytes) {
        CqlValidationRequest requestOut = new CqlValidationRequest();

        requestOut.setValidateValueSets(requestIn.isValidateValueSets());
        requestOut.setValidateCodeSystems(requestIn.isValidateCodeSystems());
        requestOut.setValidateCqlToElm(requestIn.isValidateCqlToElm());
        requestOut.setValidateSyntax(requestIn.isValidateSyntax());

        requestOut.setCql(decode(cqlBytes));

        return requestOut;
    }

    private String decode(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
