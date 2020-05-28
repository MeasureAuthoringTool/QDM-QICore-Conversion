package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.rest.dto.ConversionResultDto;
import gov.cms.mat.fhir.rest.dto.ConversionType;
import gov.cms.mat.fhir.services.components.mat.DraftMeasureXmlProcessor;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.components.mongo.ConversionResultProcessorService;
import gov.cms.mat.fhir.services.components.mongo.ConversionResultsService;
import gov.cms.mat.fhir.services.components.mongo.ThreadSessionKey;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.rest.support.FhirValidatorProcessor;
import gov.cms.mat.fhir.services.service.CqlLibraryDataService;
import gov.cms.mat.fhir.services.service.orchestration.LibraryOrchestrationService;
import gov.cms.mat.fhir.services.service.orchestration.PushLibraryService;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Min;
import java.time.Instant;

@RestController
@RequestMapping(path = "/library")
@Tag(name = "Library-Controller", description = "API for Libraries")
@Slf4j
public class StandAloneLibraryController implements FhirValidatorProcessor {
    private final ConversionResultsService conversionResultsService;
    private final PushLibraryService pushLibraryService;

    public StandAloneLibraryController(ConversionResultsService conversionResultsService,
                                       PushLibraryService pushLibraryService) {
        this.conversionResultsService = conversionResultsService;
        this.pushLibraryService = pushLibraryService;
    }

    @Operation(summary = "Orchestrate QDM to Hapi FHIR Library with the id",
            description = "Find the CQL QDM Library and convert and persist to FHIR",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Library found and results returnd"),
                    @ApiResponse(responseCode = "404", description = "CqlLibrary is not found in the mat db using the id")})
    @PostMapping("/convertStandAlone")
    public ConversionResultDto convertQdmToFhir(
            @RequestParam @Min(10) String id,
            @RequestParam ConversionType conversionType,
            @RequestParam(required = false, defaultValue = "false") boolean showWarnings,
            @RequestParam(required = false, defaultValue = "LIBRARY-QDM-ORCHESTRATION") String batchId) {

        ThreadSessionKey threadSessionKey = buildThreadSessionKey(id, conversionType, showWarnings, batchId);

        OrchestrationProperties orchestrationProperties =
                buildProperties(conversionType, showWarnings, threadSessionKey);

        return pushLibraryService.convertQdmToFhir(id, orchestrationProperties);
    }

    @Operation(summary = "Orchestrate Stand alone Hapi FHIR Library with the id",
            description = "Find the HAPI stand alone Library and persist to FHIR",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Value set found and json returned"),
                    @ApiResponse(responseCode = "404", description = "CqlLibrary is not found in the mat db using the id")})
    @PostMapping("/pushStandAloneLibrary")
    public String convertStandAloneFromMatToFhir(
            @RequestParam @Min(10) String id,
            @RequestParam(required = false, defaultValue = "LIBRARY-STANDALONE-ORCHESTRATION") String batchId) {
        ThreadSessionKey threadSessionKey = buildThreadSessionKey(id, ConversionType.CONVERSION, Boolean.FALSE, batchId);

        OrchestrationProperties orchestrationProperties =
                buildProperties(ConversionType.CONVERSION, Boolean.FALSE, threadSessionKey);


        return pushLibraryService.convertStandAloneFromMatToFhir(id, orchestrationProperties);
    }


    public OrchestrationProperties buildProperties(ConversionType conversionType,
                                                   boolean showWarnings,
                                                   ThreadSessionKey threadSessionKey) {
        return OrchestrationProperties.builder()
                .showWarnings(showWarnings)
                .conversionType(conversionType)
                .includeStdLibs(false)
                .xmlSource(XmlSource.MEASURE)
                .threadSessionKey(threadSessionKey)
                .build();
    }

    public ThreadSessionKey buildThreadSessionKey(String id,
                                                  ConversionType conversionType,
                                                  boolean showWarnings,
                                                  String batchId) {
        return ConversionReporter.setInThreadLocal("L-" + id,
                batchId,
                conversionResultsService,
                Instant.now(),
                conversionType,
                XmlSource.MEASURE,
                showWarnings,
                "");
    }
}
