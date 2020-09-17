package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.commons.model.MeasureXml;
import gov.cms.mat.fhir.services.components.reporting.ConversionReporter;
import gov.cms.mat.fhir.services.cql.parser.CqlParser;
import gov.cms.mat.fhir.services.cql.parser.CqlToMatXml;
import gov.cms.mat.fhir.services.cql.parser.CqlVisitorFactory;
import gov.cms.mat.fhir.services.exceptions.MeasureNotFoundException;
import gov.cms.mat.fhir.services.exceptions.MeasureReleaseVersionInvalidException;
import gov.cms.mat.fhir.services.repository.CqlLibraryRepository;
import gov.cms.mat.fhir.services.repository.MeasureXmlRepository;
import gov.cms.mat.fhir.services.rest.dto.CQLObject;
import gov.cms.mat.fhir.services.rest.dto.LibraryErrors;
import gov.cms.mat.fhir.services.rest.dto.ValidationRequest;
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
public class MatXmlController {

    private final MeasureXmlRepository measureXmlRepo;
    private final CqlLibraryRepository cqlLibRepo;
    private final CqlVisitorFactory visitorFactory;
    private final CqlParser cqlParser;
    private final ValidationOrchestrationService validationOrchestrationService;
    private final MeasureDataService measureDataService;

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

    public MatXmlController(MeasureXmlRepository measureXmlRepo,
                            CqlLibraryRepository cqlLibRepo,
                            CqlVisitorFactory visitorFactory,
                            CqlParser cqlParser,
                            ValidationOrchestrationService validationOrchestrationService, MeasureDataService measureDataService) {
        this.measureXmlRepo = measureXmlRepo;
        this.cqlLibRepo = cqlLibRepo;
        this.visitorFactory = visitorFactory;
        this.cqlParser = cqlParser;
        this.validationOrchestrationService = validationOrchestrationService;
        this.measureDataService = measureDataService;
    }

    @PutMapping("/standalone-lib/{id}")
    public @ResponseBody
    MatXmlResponse fromStandaloneLib(@RequestHeader(value = "UMLS-TOKEN", required = false) String ulmsToken,
                                     @NotBlank @PathVariable("id") String libId,
                                     @Valid @RequestBody MatXmlReq matXmlReq) {
        try {
            Optional<CqlLibrary> optionalLib = cqlLibRepo.findById(libId);

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
                        null);
            } else {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "CQL_LIBRARY not found for CQL_LIBRARY.id " + libId + "."
                );
            }
        } catch (RuntimeException e) {
            log.error("fromStandaloneLib", e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unexpected error in fromStandaloneLib(" + ulmsToken + "," + libId + "," + matXmlReq,
                    e);
        }
    }

    @PutMapping("/measure/{id}")
    public @ResponseBody
    MatXmlResponse fromMeasure(@RequestHeader(value = "UMLS-TOKEN", required = false) String ulmsToken,
                               @NotBlank @PathVariable("id") String measureId,
                               @Valid @RequestBody MatXmlReq matXmlReq) {
        try {


            Optional<MeasureXml> optMeasureXml = measureXmlRepo.findByMeasureId(measureId);

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
                        measureId);
            } else {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "MEASURE not found for MEASURE.id " + measureId + "."
                );
            }
        } catch (RuntimeException e) {
            log.error("fromMeasure", e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unexpected error in fromMeasure(" + ulmsToken + "," + measureId + "," + measureId,
                    e);
        }
    }

    @PutMapping("/cql")
    public @ResponseBody
    MatXmlResponse fromCql(@RequestHeader(value = "UMLS-TOKEN", required = false) String umlsToken,
                           @Valid @RequestBody MatCqlXmlReq matCqlXmlReq) {
        log.debug("MatXmlController::fromCql -> enter {}", matCqlXmlReq);
        String cql = matCqlXmlReq.getCql();
        try {
            MatXmlResponse resp = run(umlsToken,
                    matCqlXmlReq.getCql(),
                    matCqlXmlReq.getSourceModel(),
                    matCqlXmlReq,
                    null);
            log.debug("MatXmlController::fromCql -> exit {}", resp);
            return resp;
        } catch (RuntimeException e) {
            log.error("fromCql", e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unexpected error in fromMeasure(" + umlsToken + "," + cql + "," + matCqlXmlReq,
                    e);
        }
    }

    private MatXmlResponse run(String umlsToken,
                               String cql,
                               @Null CQLModel sourceModel,
                               MatXmlReq req,
                               String measureId) {
        MatXmlResponse matXmlResponse = new MatXmlResponse();
        CqlToMatXml cqlToMatXml = visitorFactory.getCqlToMatXmlVisitor();
        cqlToMatXml.setSourceModel(sourceModel);
        cqlToMatXml.setUmlsToken(umlsToken);
        cqlParser.parse(cql, cqlToMatXml);

        if (measureId != null){
            Measure measure = find(measureId);
            if (measure.getDescription() != null && measure.getDescription().contains("_")) {
                matXmlResponse.setErrors(getMeasureValidations(measure));
            }
            if (sourceModel != null && sourceModel.getLibraryName().contains("_")) {
                matXmlResponse.getErrors().addAll(getLibraryValidations(sourceModel));
            }
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
                            req.getValidationRequest());
            matXmlResponse.getErrors().addAll(libraryErrors);

            if (req.getValidationRequest().isValidateReturnType()) {
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
        CQLError cqlError = new CQLError();
        cqlError.setErrorMessage("Measure name must not contain '_' (underscore)");
        cqlError.setSeverity("Severe");
        errors.add(cqlError);
        libraryErrors.setErrors(errors);
        libraryErrorsList.add(libraryErrors);
        return libraryErrorsList;
    }

    private @NotNull List<LibraryErrors> getLibraryValidations(CQLModel sourceModel) {
        @NotNull List<LibraryErrors> libraryErrorsList = new ArrayList<>();
        LibraryErrors libraryErrors = new LibraryErrors(sourceModel.getLibraryName(), sourceModel.getVersionUsed());
        List<CQLError> errors = new ArrayList<>();
        CQLError cqlError = new CQLError();
        cqlError.setErrorMessage("Library name must not contain '_' (underscore)");
        cqlError.setSeverity("Severe");
        errors.add(cqlError);
        libraryErrors.setErrors(errors);
        libraryErrorsList.add(libraryErrors);
        return libraryErrorsList;
    }

}
