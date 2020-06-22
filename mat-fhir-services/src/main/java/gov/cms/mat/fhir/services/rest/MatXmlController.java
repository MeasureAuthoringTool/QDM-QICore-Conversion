package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.commons.model.MeasureXml;
import gov.cms.mat.fhir.services.cql.parser.CqlParser;
import gov.cms.mat.fhir.services.cql.parser.CqlToMatXml;
import gov.cms.mat.fhir.services.cql.parser.CqlVisitorFactory;
import gov.cms.mat.fhir.services.repository.CqlLibraryRepository;
import gov.cms.mat.fhir.services.repository.MeasureXmlRepository;
import gov.cms.mat.fhir.services.rest.dto.LibraryErrors;
import gov.cms.mat.fhir.services.rest.dto.ValidationRequest;
import gov.cms.mat.fhir.services.service.ValidationOrchestrationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import mat.model.cql.CQLModel;
import mat.server.CQLUtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
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

@RestController
@RequestMapping(path = "/cql-xml-gen")
@Tag(name = "MatXmlController", description = "API for validating cql")
@Slf4j
@Controller
public class MatXmlController {
    @PutMapping("/cql")
    public @ResponseBody
    MatXmlResponse fromCql(@RequestHeader(value = "UMLS-TOKEN", required=false) String umlsToken,
                           @Valid @RequestBody MatCqlXmlReq matCqlXmlReq) {
        log.debug("MatXmlController::fromCql -> enter {}", matCqlXmlReq);

        try {
            MatXmlResponse resp = run(umlsToken,
                    matCqlXmlReq.getCql(),
                    matCqlXmlReq.getSourceModel(),
                    matCqlXmlReq);
            log.debug("MatXmlController::fromCql -> exit {}", resp);
            return resp;
        } catch (RuntimeException e) {
            log.error("fromCql", e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unexpected error in fromMeasure(" + umlsToken + "," + matCqlXmlReq.getCql() + "," + matCqlXmlReq,
                    e);
        }
    }

    private final MeasureXmlRepository measureXmlRepo;

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    public static class MatXmlResponse {
        @NotNull
        private List<LibraryErrors> errors = new ArrayList<>();
        @NotNull
        private CQLModel cqlModel;
        @NotBlank
        private String cql;
    }

    private final CqlLibraryRepository cqlLibRepo;
    private final CqlVisitorFactory visitorFactory;
    private final CqlParser cqlParser;
    private final ValidationOrchestrationService validationOrchestrationService;

    public MatXmlController(MeasureXmlRepository measureXmlRepo,
                            CqlLibraryRepository cqlLibRepo,
                            CqlVisitorFactory visitorFactory,
                            CqlParser cqlParser,
                            ValidationOrchestrationService validationOrchestrationService) {
        this.measureXmlRepo = measureXmlRepo;
        this.cqlLibRepo = cqlLibRepo;
        this.visitorFactory = visitorFactory;
        this.cqlParser = cqlParser;
        this.validationOrchestrationService = validationOrchestrationService;
    }

    @PutMapping("/standalone-lib/{id}")
    public @ResponseBody
    MatXmlResponse fromStandaloneLib(@RequestHeader(value = "UMLS-TOKEN", required=false) String ulmsToken,
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
                String cql = CQLUtilityClass.getCqlString(model,"").getLeft();

                return run(ulmsToken,
                        cql,
                        model,
                        matXmlReq);
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
    MatXmlResponse fromMeasure(@RequestHeader(value = "UMLS-TOKEN", required=false) String ulmsToken,
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
                String cql = CQLUtilityClass.getCqlString(model,"").getLeft();

                return run(ulmsToken,
                        cql,
                        model,
                        matXmlReq);
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

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    public static class MatCqlXmlReq extends MatXmlReq {
        @NotBlank
        private String cql;
        private CQLModel sourceModel;
    }

    @Data
    @NoArgsConstructor
    @ToString
    public static class MatXmlReq {
        private boolean isLinting = true;
        @Valid
        private ValidationRequest validationRequest;
    }

    private MatXmlResponse run(String umlsToken,
                               String cql,
                               @Null CQLModel sourceModel,
                               MatXmlReq req) {
        MatXmlResponse matXmlResponse = new MatXmlResponse();

        CqlToMatXml cqlToMatXml = visitorFactory.getCqlToMatXmlVisitor();
        cqlToMatXml.setSourceModel(sourceModel);
        cqlToMatXml.setUmlsToken(umlsToken);
        cqlParser.parse(cql, cqlToMatXml);

        matXmlResponse.setCql(cql);
        matXmlResponse.setCqlModel(cqlToMatXml.getDestinationModel());

        if (sourceModel != null) {
            // Overwrite fields the user is not allowed to change for FHIR.
            matXmlResponse.getCqlModel().setLibraryName(sourceModel.getLibraryName());
            matXmlResponse.getCqlModel().setUsingModelVersion(sourceModel.getUsingModelVersion());
            matXmlResponse.getCqlModel().setUsingModel(sourceModel.getUsingModel());
            matXmlResponse.getCqlModel().setVersionUsed(sourceModel.getVersionUsed());
        }

        if (!cqlToMatXml.getErrors().isEmpty()) {
            LibraryErrors libraryErrors = new LibraryErrors();
            libraryErrors.setErrors(cqlToMatXml.getErrors());
            libraryErrors.setName(matXmlResponse.getCqlModel().getLibraryName());
            libraryErrors.setVersion(matXmlResponse.getCqlModel().getVersionUsed());
            matXmlResponse.setErrors(Collections.singletonList(libraryErrors));
        } else {
            List<LibraryErrors> libraryErrors =
                    validationOrchestrationService.validateCql(cql,
                            matXmlResponse.getCqlModel(),
                            umlsToken,
                            req.getValidationRequest());
            matXmlResponse.setErrors(libraryErrors);
        }

        return matXmlResponse;
    }
}
