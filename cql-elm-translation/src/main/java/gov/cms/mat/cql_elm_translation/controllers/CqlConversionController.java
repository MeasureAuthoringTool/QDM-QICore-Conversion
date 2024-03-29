package gov.cms.mat.cql_elm_translation.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gov.cms.mat.cql.dto.CqlConversionPayload;
import gov.cms.mat.cql_elm_translation.data.RequestData;
import gov.cms.mat.cql_elm_translation.service.CqlConversionService;
import gov.cms.mat.cql_elm_translation.service.MatXmlConversionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.cqframework.cql.cql2elm.LibraryBuilder;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.UncheckedIOException;

@RestController
@RequestMapping(path = "/cql/translator")
@Tag(name = "Conversion-Controller", description = "API for converting stuff - TODO.")
@Slf4j
public class CqlConversionController {
    private final CqlConversionService cqlConversionService;
    private final MatXmlConversionService matXmlConversionService;

    public CqlConversionController(CqlConversionService cqlConversionService,
                                   MatXmlConversionService matXmlConversionService) {
        this.cqlConversionService = cqlConversionService;
        this.matXmlConversionService = matXmlConversionService;
    }

    @PutMapping(path = "/cql", consumes = "text/plain", produces = "application/elm+json")
    public CqlConversionPayload cqlToElmJson(
            @RequestBody String cqlData,
            @RequestParam(required = false) LibraryBuilder.SignatureLevel signatures,
            @RequestParam(defaultValue = "false") Boolean showWarnings,
            @RequestParam(defaultValue = "true") Boolean annotations,
            @RequestParam(defaultValue = "true") Boolean locators,
            @RequestParam(value = "disable-list-demotion", defaultValue = "true") Boolean disableListDemotion,
            @RequestParam(value = "disable-list-promotion", defaultValue = "true") Boolean disableListPromotion,
            @RequestParam(value = "disable-method-invocation", defaultValue = "true") Boolean disableMethodInvocation,
            @RequestParam(value = "validate-units", defaultValue = "true") Boolean validateUnits) {
        RequestData requestData = RequestData.builder()
                .cqlData(cqlData)
                .showWarnings(showWarnings)
                .signatures(signatures)
                .annotations(annotations)
                .locators(locators)
                .disableListDemotion(disableListDemotion)
                .disableListPromotion(disableListPromotion)
                .disableMethodInvocation(disableMethodInvocation)
                .validateUnits(validateUnits)
                .build();

        cqlConversionService.setUpMatLibrarySourceProvider(cqlData);

        CqlConversionPayload payload = cqlConversionService.processCqlDataWithErrors(requestData);
        TranslatorOptionsRemover remover = new TranslatorOptionsRemover(payload.getJson());
        String cleanedJson =  remover.clean();
        payload.setJson(cleanedJson);
        return payload;
    }

    @PutMapping(path = "/xml", consumes = "text/plain", produces = "application/elm+json")
    public String xmlToElmJson(
            @RequestBody String xml,
            @RequestParam(defaultValue = "false") Boolean showWarnings,

            @RequestParam(defaultValue = "true") Boolean isDraft,


            @RequestParam(required = false) LibraryBuilder.SignatureLevel signatures,
            @RequestParam(defaultValue = "true") Boolean annotations,
            @RequestParam(defaultValue = "true") Boolean locators,
            @RequestParam(value = "disable-list-demotion", defaultValue = "true") Boolean disableListDemotion,
            @RequestParam(value = "disable-list-promotion", defaultValue = "true") Boolean disableListPromotion,
            @RequestParam(value = "disable-method-invocation", defaultValue = "true") Boolean disableMethodInvocation,
            @RequestParam(value = "validate-units", defaultValue = "true") Boolean validateUnits) {

        String cqlData = matXmlConversionService.processCqlXml(xml);

        cqlConversionService.setUpMatLibrarySourceProvider(cqlData);

        RequestData requestData = RequestData.builder()
                .cqlData(cqlData)
                .showWarnings(showWarnings)
                .signatures(signatures)
                .annotations(annotations)
                .locators(locators)
                .disableListDemotion(disableListDemotion)
                .disableListPromotion(disableListPromotion)
                .disableMethodInvocation(disableMethodInvocation)
                .validateUnits(validateUnits)
                .build();

        CqlConversionPayload payload =  cqlConversionService.processCqlDataWithErrors(requestData);
        TranslatorOptionsRemover remover = new TranslatorOptionsRemover(payload.getJson());
        return remover.clean();
    }

    /**
     * Removes this node, when present, which blows up array processing for annotation array.
     * {
     * "translatorOptions": "DisableMethodInvocation,EnableLocators,DisableListPromotion,EnableDetailedErrors,EnableAnnotations,DisableListDemotion",
     * "type": "CqlToElmInfo"
     * },
     */
    static class TranslatorOptionsRemover {
        final String json;

        TranslatorOptionsRemover(String json) {
            this.json = json;
        }

        String clean() {

            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(json);
                JsonNode libraryNode = rootNode.get("library");
                JsonNode annotationNode = libraryNode.get("annotation");

                if (annotationNode == null || annotationNode.isMissingNode()) {
                    return json;
                }

                if (annotationNode.isEmpty() &&
                        libraryNode instanceof ObjectNode) {
                    ObjectNode objectNode = (ObjectNode) libraryNode;
                    objectNode.remove("annotation");
                    return rootNode.toPrettyString();
                }

                for (int i = 0; i < annotationNode.size(); i++) {
                    if(annotationNode.get(i).has("translatorOptions")) {
                        ((ArrayNode)annotationNode).remove(i);
                        break;
                    }
                }

                return rootNode.toPrettyString();

            } catch (JsonProcessingException e) {
                throw new UncheckedIOException(e);
            }

        }

    }
}
