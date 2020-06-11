package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.commons.model.CqlLibraryExport;
import gov.cms.mat.fhir.commons.model.MeasureExport;
import gov.cms.mat.fhir.commons.model.MeasureXml;
import gov.cms.mat.fhir.services.cql.parser.CqlParser;
import gov.cms.mat.fhir.services.cql.parser.CqlToMatXml;
import gov.cms.mat.fhir.services.cql.parser.CqlVisitorFactory;
import gov.cms.mat.fhir.services.repository.CqlLibraryExportRepository;
import gov.cms.mat.fhir.services.repository.CqlLibraryRepository;
import gov.cms.mat.fhir.services.repository.MeasureExportRepository;
import gov.cms.mat.fhir.services.repository.MeasureXmlRepository;
import gov.cms.mat.fhir.services.rest.dto.LibraryErrors;
import gov.cms.mat.fhir.services.rest.dto.ValidationRequest;
import gov.cms.mat.fhir.services.service.ValidationOrchestrationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mat.model.cql.CQLModel;
import mat.server.service.impl.XMLMarshalUtil;
import mat.server.util.XmlProcessor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/cql-xml-gen")
@Tag(name = "MatXmlController", description = "API for validating cql")
@Slf4j
@Controller
public class MatXmlController {
    @Data
    @NoArgsConstructor
    public static class MatXmlResponse {
        @NotNull
        private List<LibraryErrors> errors = new ArrayList<>();
        @NotNull
        private CQLModel cqlModel;
        @NotBlank
        private String cql;
    }

    private final MeasureXmlRepository measureXmlRepo;

    @Data
    @NoArgsConstructor
    public static class MatCqlXmlReq {
        @NotBlank
        private String cql;
        @Valid
        private MatXmlReq xmlReq;
    }

    private final MeasureExportRepository measureExportRepo;
    private final CqlLibraryRepository cqlLibRepo;
    private final CqlLibraryExportRepository cqlLibExportRepo;
    private final CqlVisitorFactory visitorFactory;
    private final CqlParser cqlParser;
    private final ValidationOrchestrationService validationOrchestrationService;
    private XMLMarshalUtil xmlMarshalUtil = new XMLMarshalUtil();

    public MatXmlController(
                            MeasureXmlRepository measureXmlRepo,
                            MeasureExportRepository measureExportRepo,
                            CqlLibraryRepository cqlLibRepo,
                            CqlLibraryExportRepository cqlLibExportRepo,
                            CqlVisitorFactory visitorFactory,
                            @Qualifier("antlCqlParser") CqlParser cqlParser,
                            ValidationOrchestrationService validationOrchestrationService) {
        this.measureXmlRepo = measureXmlRepo;
        this.cqlLibRepo = cqlLibRepo;
        this.cqlLibExportRepo = cqlLibExportRepo;
        this.measureExportRepo = measureExportRepo;
        this.visitorFactory = visitorFactory;
        this.cqlParser = cqlParser;
        this.validationOrchestrationService = validationOrchestrationService;
    }

    private MatXmlResponse run(String umlsToken,
                               String existingCql,
                               @Null CQLModel existingModel,
                               MatXmlReq req) {
        MatXmlResponse matXmlResponse = new MatXmlResponse();

        CqlToMatXml cqlToMatXml = visitorFactory.getCqlToMatXmlVisitor();
        cqlToMatXml.setSourceModel(existingModel);
        cqlToMatXml.setUmlsToken(umlsToken);
        cqlParser.parse(existingCql, cqlToMatXml);

        matXmlResponse.setCql(existingCql);
        CQLModel newModel = cqlToMatXml.getDestinationModel();
        matXmlResponse.setCqlModel(newModel);

        if (existingModel != null) {
            // Overwrite fields the user is not allowed to change for FHIR.
            newModel.setLibraryName(existingModel.getLibraryName());
            newModel.setUsingModelVersion(existingModel.getUsingModelVersion());
            newModel.setUsingModel(existingModel.getUsingModel());
            newModel.setVersionUsed(existingModel.getVersionUsed());
        }

        if (!cqlToMatXml.getErrors().isEmpty()) {
            LibraryErrors libraryErrors = new LibraryErrors();
            libraryErrors.setErrors(cqlToMatXml.getErrors());
            libraryErrors.setName(newModel.getLibraryName());
            libraryErrors.setVersion(newModel.getVersionUsed());
            libraryErrors.setName(newModel.getLibraryName());
            matXmlResponse.setErrors(Arrays.asList(libraryErrors));
        } else {
            List<LibraryErrors> libraryErrors =
                    validationOrchestrationService.validateCql(existingCql,
                            existingModel,
                            umlsToken,
                            req.getValidationRequest());
            matXmlResponse.setErrors(libraryErrors);
        }

        return matXmlResponse;
    }

    @GetMapping("/standalone-lib/{id}")
    public @ResponseBody
    MatXmlResponse fromStandaloneLib(@NotBlank @RequestHeader(value = "ULMS-TOKEN") String ulmsToken,
                                     @NotBlank @PathVariable("id") String libId,
                                     @RequestParam(defaultValue = "true") MatXmlReq matXmlReq) {
        try {
            Optional<CqlLibrary> optionalLib = cqlLibRepo.findById(libId);

            if (optionalLib.isPresent()) {
                CqlLibrary lib = optionalLib.get();
                if (StringUtils.isBlank(lib.getCqlXml())) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "CQL_LIBRARY.CQL_XML does not exist for CQL_LIBRARY.id " + libId + "."
                    );
                }
                CqlLibraryExport export = cqlLibExportRepo.getCqlLibraryExportByCqlLibraryId(libId);
                if (export == null) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "CQL_LIBRARY_EXPORT does not exist for CQL_LIBRARY.id " + libId + "."
                    );
                }
                byte[] cqlXml = export.getCql();
                if (cqlXml == null) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "CQL_LIBRARY_EXPORT.CQL does not exist for CQL_LIBRARY.id " + libId + "."
                    );
                }
                return run(ulmsToken,
                        decode(cqlXml),
                        convert(lib.getCqlXml()),
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

    @GetMapping("/measure/{id}")
    public @ResponseBody
    MatXmlResponse fromMeasure(@NotBlank @RequestHeader(value = "ULMS-TOKEN") String ulmsToken,
                               @NotBlank @PathVariable("id") String measureId,
                               @RequestParam(defaultValue = "true") MatXmlReq matXmlReq) {
        try {
            Optional<MeasureXml> optMeasureXml = measureXmlRepo.findByMeasureId(measureId);

            if (optMeasureXml.isPresent()) {
                byte[] measureXmlBytes = optMeasureXml.get().getMeasureXml();
                if (measureXmlBytes == null) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "MEASURE_XML.XML does not exist for MEASURE.ID " + measureId + "."
                    );
                }
                MeasureExport export = measureExportRepo.getMeasureExportById(measureId);
                if (export == null) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "MEASURE_EXPORT does not exist for MEASURE.id " + measureId + "."
                    );
                }
                byte[] cqlXml = export.getCql();
                if (cqlXml == null) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "MEASURE_EXPORT.CQL does not exist for MEASURE.id " + measureId + "."
                    );
                }

                return run(ulmsToken,
                        decode(export.getCql()),
                        convert(decode(measureXmlBytes)),
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

    @PutMapping("/cql")
    public @ResponseBody
    MatXmlResponse fromCql(@NotBlank @RequestHeader(value = "ULMS-TOKEN") String umlsToken,
                           @Valid @RequestBody MatCqlXmlReq matCqlXmlReq) {
        String cql = matCqlXmlReq.getCql();
        MatXmlReq matXmlReq = matCqlXmlReq.getXmlReq();
        try {
            return run(umlsToken,
                    cql,
                    null,
                    matCqlXmlReq.getXmlReq());
        } catch (RuntimeException e) {
            log.error("fromCql", e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unexpected error in fromMeasure(" + umlsToken + "," + cql + "," + matXmlReq,
                    e);
        }
    }

    @Data
    @NoArgsConstructor
    public static class MatXmlReq {
        private boolean isLinting = true;
        @Valid
        private ValidationRequest validationRequest;
    }

    private String decode(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private CQLModel convert(String measureXml) {
        try {
            XmlProcessor measureXMLProcessor = new XmlProcessor(measureXml);
            String cqlXmlFrag = measureXMLProcessor.getXmlByTagName("cqlLookUp");
            return (CQLModel) xmlMarshalUtil.convertXMLToObject("CQLModelMapping.xml", cqlXmlFrag, CQLModel.class);
        } catch (Throwable t) {
            throw new RuntimeException("Error converting measure XML to CQLModel", t);
        }
    }
}
