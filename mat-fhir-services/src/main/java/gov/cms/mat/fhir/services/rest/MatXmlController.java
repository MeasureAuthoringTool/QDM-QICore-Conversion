package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.commons.model.MeasureXml;
import gov.cms.mat.fhir.services.components.reporting.ConversionReporter;
import gov.cms.mat.fhir.services.components.validation.unused.UnusedValidator;
import gov.cms.mat.fhir.services.cql.parser.CqlParser;
import gov.cms.mat.fhir.services.cql.parser.CqlToMatXml;
import gov.cms.mat.fhir.services.cql.parser.CqlVisitorFactory;
import gov.cms.mat.fhir.services.exceptions.MeasureNotFoundException;
import gov.cms.mat.fhir.services.exceptions.MeasureReleaseVersionInvalidException;
import gov.cms.mat.fhir.services.repository.CqlLibraryRepository;
import gov.cms.mat.fhir.services.repository.MeasureXmlRepository;
import gov.cms.mat.fhir.services.rest.dto.CQLObject;
import gov.cms.mat.fhir.services.rest.dto.LibraryErrors;
import gov.cms.mat.fhir.services.rest.dto.UnusedCqlElements;
import gov.cms.mat.fhir.services.rest.dto.ValidationRequest;
import gov.cms.mat.fhir.services.rest.support.ApiKeyResponseHeader;
import gov.cms.mat.fhir.services.service.MeasureDataService;
import gov.cms.mat.fhir.services.service.ValidationOrchestrationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import mat.model.cql.CQLModel;
import mat.server.CQLUtilityClass;
import mat.shared.CQLError;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static gov.cms.mat.fhir.rest.dto.ConversionOutcome.MEASURE_NOT_FOUND;
import static gov.cms.mat.fhir.rest.dto.ConversionOutcome.MEASURE_RELEASE_VERSION_INVALID;

@RestController
@RequestMapping(path = "/cql-xml-gen")
@Tag(name = "MatXmlController", description = "API for validating cql")
@Slf4j
@Controller
public class MatXmlController implements ApiKeyResponseHeader {

    private final MeasureXmlRepository measureXmlRepository;
    private final CqlLibraryRepository cqlLibraryRepository;
    private final CqlVisitorFactory cqlVisitorFactory;
    private final CqlParser cqlParser;
    private final ValidationOrchestrationService validationOrchestrationService;
    private final MeasureDataService measureDataService;

    public MatXmlController(MeasureXmlRepository measureXmlRepository,
                            CqlLibraryRepository cqlLibraryRepository,
                            CqlVisitorFactory cqlVisitorFactory,
                            CqlParser cqlParser,
                            ValidationOrchestrationService validationOrchestrationService, MeasureDataService measureDataService) {
        this.measureXmlRepository = measureXmlRepository;
        this.cqlLibraryRepository = cqlLibraryRepository;
        this.cqlVisitorFactory = cqlVisitorFactory;
        this.cqlParser = cqlParser;
        this.validationOrchestrationService = validationOrchestrationService;
        this.measureDataService = measureDataService;
    }

    @PutMapping("/standalone-lib/{id}")
    public @ResponseBody
    MatXmlResponse fromStandaloneLib(@RequestHeader(value = "UMLS-TOKEN", required = false) String ulmsToken,
                                     @RequestHeader(value = "API-KEY", required = false) String apiKey,
                                     @NotBlank @PathVariable("id") String libId,
                                     @Valid @RequestBody MatXmlReq matXmlReq,
                                     HttpServletResponse response) {

        try {
            Optional<CqlLibrary> optionalLib = cqlLibraryRepository.findById(libId);

            if (optionalLib.isPresent()) {
                CqlLibrary lib = optionalLib.get();
                if (StringUtils.isBlank(lib.getCqlXml())) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "CQL_LIBRARY.CQL_XML does not exist for CQL_LIBRARY.id " + libId + "."
                    );
                }

                CQLModel model = CQLUtilityClass.getCQLModelFromXML(lib.getCqlXml());
                String cql = CQLUtilityClass.getCqlString(model, "").getLeft();

                return run(ulmsToken,
                        cql,
                        model,
                        matXmlReq,
                        null,
                        apiKey);
            } else {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "CQL_LIBRARY not found for CQL_LIBRARY.id " + libId + "."
                );
            }
        } finally {
            processResponseHeader(response);
        }
    }

    @PutMapping("/measure/{id}")
    public @ResponseBody
    MatXmlResponse fromMeasure(@RequestHeader(value = "UMLS-TOKEN", required = false) String ulmsToken,
                               @RequestHeader(value = "API-KEY", required = false) String apiKey,
                               @NotBlank @PathVariable("id") String measureId,
                               @Valid @RequestBody MatXmlReq matXmlReq,
                               HttpServletResponse response) {

        try {
            Optional<MeasureXml> optMeasureXml = measureXmlRepository.findByMeasureId(measureId);

            if (optMeasureXml.isPresent()) {
                byte[] measureXmlBytes = optMeasureXml.get().getMeasureXml();
                if (measureXmlBytes == null) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "MEASURE_XML.XML does not exist for MEASURE.ID " + measureId + "."
                    );
                }
                String matXml = new String(measureXmlBytes, StandardCharsets.UTF_8);
                CQLModel model = CQLUtilityClass.getCQLModelFromXML(matXml);
                String cql = CQLUtilityClass.getCqlString(model, "").getLeft();

                return run(ulmsToken,
                        cql,
                        model,
                        matXmlReq,
                        measureId,
                        apiKey);
            } else {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "MEASURE not found for MEASURE.id " + measureId + "."
                );
            }
        } finally {
            processResponseHeader(response);
        }
    }

    @PutMapping("/cql")
    public @ResponseBody
    MatXmlResponse fromCql(@RequestHeader(value = "UMLS-TOKEN", required = false) String umlsToken,
                           @RequestHeader(value = "API-KEY", required = false) String apiKey,
                           @Valid @RequestBody MatCqlXmlReq matCqlXmlReq,
                           HttpServletResponse response) {
        try {
            log.trace("MatXmlController::fromCql -> enter {}", matCqlXmlReq);

            MatXmlResponse resp = run(umlsToken,
                    matCqlXmlReq.getCql(),
                    matCqlXmlReq.getSourceModel(),
                    matCqlXmlReq,
                    null,
                    apiKey);
            log.trace("MatXmlController::fromCql -> exit {}", resp);

            ValidationRequest validationRequest = new ValidationRequest();
            validationRequest.setValidateReturnType(true);

            if (resp.getCqlModel().isFhir()) {
                UnusedValidator unused = new UnusedValidator(resp.getCqlModel());
                resp.setUnusedCqlElements(unused.findUnused());
            }

            return resp;
        } finally {
            processResponseHeader(response);
        }
    }

    private MatXmlResponse run(String umlsToken,
                               String cql,
                               @Null CQLModel sourceModel,
                               MatXmlReq matXmlReq,
                               String measureId,
                               String apiKey) {
        MatXmlResponse matXmlResponse = new MatXmlResponse();
        CqlToMatXml cqlToMatXml = cqlVisitorFactory.getCqlToMatXmlVisitor();
        cqlToMatXml.setSourceModel(sourceModel);
        cqlToMatXml.setUmlsToken(umlsToken);
        cqlParser.parse(cql, cqlToMatXml);

        if (measureId != null) {
            Measure measure = find(measureId);
            if (measure.getDescription() != null && measure.getDescription().contains("_")) {
                matXmlResponse.setErrors(getMeasureValidations(measure));
            }
        }

        if (sourceModel != null && sourceModel.getLibraryName().contains("_")) {
            matXmlResponse.getErrors().addAll(getLibraryValidations(sourceModel));
        }

        matXmlResponse.setCql(cql);
        matXmlResponse.setCqlModel(cqlToMatXml.getDestinationModel());

        if (sourceModel != null && sourceModel.isFhir()) {
            // Overwrite fields the user is not allowed to change for FHIR.
            matXmlResponse.getCqlModel().setUsingModel(sourceModel.getUsingModel());
            matXmlResponse.getCqlModel().setVersionUsed(sourceModel.getVersionUsed());
            matXmlResponse.getCqlModel().setLibraryName(sourceModel.getLibraryName());
            matXmlResponse.getCqlModel().setUsingModelVersion(sourceModel.getUsingModelVersion());
        }

        if (!cqlToMatXml.getSeveres().isEmpty()) {
            // If there are any severe errors, we stop and don't validate any further.
            // The are intended to mean we can't save the CQL like it is now because it is invalid.
            LibraryErrors libraryErrors = new LibraryErrors();
            libraryErrors.setErrors(cqlToMatXml.getSeveres());
            libraryErrors.setName(matXmlResponse.getCqlModel().getLibraryName());
            libraryErrors.setVersion(matXmlResponse.getCqlModel().getVersionUsed());
            matXmlResponse.getErrors().add(libraryErrors);
        } else {
            // Create a library error for all preexisting errors and warnings.
            LibraryErrors preexistingErrors = new LibraryErrors();
            preexistingErrors.setErrors(cqlToMatXml.getErrors());
            preexistingErrors.getErrors().addAll(cqlToMatXml.getWarnings());
            preexistingErrors.setName(matXmlResponse.getCqlModel().getLibraryName());
            preexistingErrors.setVersion(matXmlResponse.getCqlModel().getVersionUsed());

            List<LibraryErrors> libraryErrors =
                    validationOrchestrationService.validateCql(cql,
                            matXmlResponse.getCqlModel(),
                            umlsToken,
                            Collections.singletonList(preexistingErrors),
                            matXmlReq.getValidationRequest(),
                            apiKey);
            matXmlResponse.getErrors().addAll(libraryErrors);

            if (matXmlReq.getValidationRequest().isValidateReturnType()) {
                matXmlResponse.setCqlObject(validationOrchestrationService.buildCqlObject(sourceModel));
            }
        }
        return matXmlResponse;
    }

    private Measure find(String id) {
        try {
            return measureDataService.findOneValid(id);
        } catch (MeasureReleaseVersionInvalidException e) {
            ConversionReporter.setTerminalMessage(e.getMessage(), MEASURE_RELEASE_VERSION_INVALID);
            return null;
        } catch (MeasureNotFoundException e) {
            ConversionReporter.setTerminalMessage(e.getMessage(), MEASURE_NOT_FOUND);
            return null;
        }
    }

    private @NotNull List<LibraryErrors> getMeasureValidations(Measure measure) {
        @NotNull List<LibraryErrors> libraryErrorsList = new ArrayList<>();
        LibraryErrors libraryErrors = new LibraryErrors(measure.getDescription(), measure.getVersion().toString());
        List<CQLError> errors = new ArrayList<>();
        CQLError cqlError = createError("Measure name must not contain '_' (underscore)", "Severe", 1);
        errors.add(cqlError);
        libraryErrors.setErrors(errors);
        libraryErrorsList.add(libraryErrors);
        return libraryErrorsList;
    }

    private @NotNull List<LibraryErrors> getLibraryValidations(CQLModel sourceModel) {
        @NotNull List<LibraryErrors> libraryErrorsList = new ArrayList<>();
        LibraryErrors libraryErrors = new LibraryErrors(sourceModel.getLibraryName(), sourceModel.getVersionUsed());
        List<CQLError> errors = new ArrayList<>();
        CQLError cqlError = createError("Library name must not contain '_' (underscore)", "Severe", 1);
        errors.add(cqlError);
        libraryErrors.setErrors(errors);
        libraryErrorsList.add(libraryErrors);
        return libraryErrorsList;
    }

    private CQLError createError(String msg, String sevrity, int lineNumber) {
        CQLError e = new CQLError();
        e.setSeverity(sevrity);
        e.setErrorMessage(msg);
        e.setErrorInLine(lineNumber);
        e.setErrorAtOffset(0);
        e.setStartErrorInLine(lineNumber);
        e.setEndErrorInLine(lineNumber);
        return e;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    public static class MatXmlResponse {
        @NotNull
        private List<LibraryErrors> errors = new ArrayList<>();
        @NotNull
        private CQLModel cqlModel;
        @NotBlank
        private String cql;
        @NotNull
        private CQLObject cqlObject;
        @Null
        private UnusedCqlElements unusedCqlElements;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    public static class MatCqlXmlReq extends MatXmlReq {
        @NotBlank
        private String cql;
        private CQLModel sourceModel;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    public static class MatXmlReq {
        //Currently not functional.
        private boolean isLinting = true;
        @Valid
        private ValidationRequest validationRequest;
    }

}
