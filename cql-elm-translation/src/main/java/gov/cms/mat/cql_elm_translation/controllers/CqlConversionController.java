package gov.cms.mat.cql_elm_translation.controllers;

import gov.cms.mat.cql_elm_translation.data.RequestData;
import gov.cms.mat.cql_elm_translation.service.CqlConversionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.cqframework.cql.cql2elm.LibraryBuilder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/cql/translator")
@Tag(name = "Conversion-Controller", description = "API for converting stuff - TODO.")
@Slf4j
public class CqlConversionController {
    private final CqlConversionService cqlConversionService;

    public CqlConversionController(CqlConversionService cqlConversionService) {
        this.cqlConversionService = cqlConversionService;
    }

    @PostMapping(path = "/cql", consumes = "text/plain", produces = "application/elm+json")
    public String cqlToElmJson(
            @RequestBody String cqlData,
            @RequestParam(required = false) LibraryBuilder.SignatureLevel signatures,
            @RequestParam(value = "restrict-data-model", required = false) String restrictDataModel,
            @RequestParam(defaultValue = "true") Boolean annotations,
            @RequestParam(defaultValue = "true") Boolean locators,
            @RequestParam(value = "disable-list-demotion", defaultValue = "true") Boolean disableListDemotion,
            @RequestParam(value = "disable-list-promotion", defaultValue = "true") Boolean disableListPromotion,
            @RequestParam(value = "disable-method-invocation", defaultValue = "true") Boolean disableMethodInvocation,
            @RequestParam(value = "validate-units", defaultValue = "true") Boolean validateUnits) {

        RequestData requestData = RequestData.builder()
                .cqlData(cqlData)
                .signatures(signatures)
                .restrictDataModel(restrictDataModel)
                .annotations(annotations)
                .locators(locators)
                .disableListDemotion(disableListDemotion)
                .disableListPromotion(disableListPromotion)
                .disableMethodInvocation(disableMethodInvocation)
                .validateUnits(validateUnits)
                .build();

        return cqlConversionService.processCqlData(requestData);
    }

    @PostMapping(path = "/xml", consumes = "text/plain", produces = "application/elm+json")
    public String xmlToElmJson(
            @RequestBody String xml,
            @RequestParam(required = false) LibraryBuilder.SignatureLevel signatures,
            @RequestParam(value = "restrict-data-model", required = false) String restrictDataModel,
            @RequestParam(defaultValue = "true") Boolean annotations,
            @RequestParam(defaultValue = "true") Boolean locators,
            @RequestParam(value = "disable-list-demotion", defaultValue = "true") Boolean disableListDemotion,
            @RequestParam(value = "disable-list-promotion", defaultValue = "true") Boolean disableListPromotion,
            @RequestParam(value = "disable-method-invocation", defaultValue = "true") Boolean disableMethodInvocation,
            @RequestParam(value = "validate-units", defaultValue = "true") Boolean validateUnits) {

        String cqlData = cqlConversionService.processCqlXml(xml);

        RequestData requestData = RequestData.builder()
                .cqlData(cqlData)
                .signatures(signatures)
                .restrictDataModel(restrictDataModel)
                .annotations(annotations)
                .locators(locators)
                .disableListDemotion(disableListDemotion)
                .disableListPromotion(disableListPromotion)
                .disableMethodInvocation(disableMethodInvocation)
                .validateUnits(validateUnits)
                .build();

        return cqlConversionService.processCqlData(requestData);
    }
}
