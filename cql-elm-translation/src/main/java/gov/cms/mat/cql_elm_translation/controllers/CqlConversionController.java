package gov.cms.mat.cql_elm_translation.controllers;

import gov.cms.mat.cql_elm_translation.data.RequestData;
import gov.cms.mat.cql_elm_translation.service.CqlConversionService;
import gov.cms.mat.cql_elm_translation.service.MatXmlConversionService;
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
    private final MatXmlConversionService matXmlConversionService;

    public CqlConversionController(CqlConversionService cqlConversionService,
                                   MatXmlConversionService matXmlConversionService) {
        this.cqlConversionService = cqlConversionService;
        this.matXmlConversionService = matXmlConversionService;
    }

    @PutMapping(path = "/cql", consumes = "text/plain", produces = "application/elm+json")
    public String cqlToElmJson(
            @RequestBody String cqlData,
            @RequestParam(required = false) LibraryBuilder.SignatureLevel signatures,
            @RequestParam(defaultValue = "true") Boolean annotations,
            @RequestParam(defaultValue = "true") Boolean locators,
            @RequestParam(value = "disable-list-demotion", defaultValue = "true") Boolean disableListDemotion,
            @RequestParam(value = "disable-list-promotion", defaultValue = "true") Boolean disableListPromotion,
            @RequestParam(value = "disable-method-invocation", defaultValue = "true") Boolean disableMethodInvocation,
            @RequestParam(value = "validate-units", defaultValue = "true") Boolean validateUnits) {
        RequestData requestData = RequestData.builder()
                .cqlData(cqlData)
                .signatures(signatures)
                .annotations(annotations)
                .locators(locators)
                .disableListDemotion(disableListDemotion)
                .disableListPromotion(disableListPromotion)
                .disableMethodInvocation(disableMethodInvocation)
                .validateUnits(validateUnits)
                .build();

        cqlConversionService.setUpMatLibrarySourceProvider(cqlData);

        String json = cqlConversionService.processCqlDataWithErrors(requestData);
        TranslatorOptionsRemover remover = new TranslatorOptionsRemover(json);
        return remover.clean();
    }

    @PutMapping(path = "/xml", consumes = "text/plain", produces = "application/elm+json")
    public String xmlToElmJson(
            @RequestBody String xml,
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
                .signatures(signatures)
                .annotations(annotations)
                .locators(locators)
                .disableListDemotion(disableListDemotion)
                .disableListPromotion(disableListPromotion)
                .disableMethodInvocation(disableMethodInvocation)
                .validateUnits(validateUnits)
                .build();

        String json = cqlConversionService.processCqlDataWithErrors(requestData);
        TranslatorOptionsRemover remover = new TranslatorOptionsRemover(json);
        return remover.clean();
    }

    /**
     * Removes this node which blows up array processing for annotation array.
     * {
     * "translatorOptions": "DisableMethodInvocation,EnableLocators,DisableListPromotion,EnableDetailedErrors,EnableAnnotations,DisableListDemotion",
     * "type": "CqlToElmInfo"
     * },
     */
    private static class TranslatorOptionsRemover {
        private final static String START_TAG = "translatorOptions\" :";
        private final static String END_TAG = "},";

        final String json;

        TranslatorOptionsRemover(String json) {
            this.json = json;
        }

        private String clean() {
            int tagStart = json.indexOf(START_TAG);

            if (tagStart < 0) {
                return json;
            }

            int start = findOpenBracket(tagStart);
            int end = json.indexOf(END_TAG, tagStart);

            if (end < 0) {
                throw new IllegalArgumentException("Cannot find close bracket");
            }

            String translatorOptionTag = json.substring(start, end + END_TAG.length());

            String cleanedJson = json.replace(translatorOptionTag, "");

            log.debug(cleanedJson);

            return cleanedJson;
        }

        private int findOpenBracket(int start) {
            while (start > 0) {
                if (json.charAt(start) == '{') {
                    return start;
                }
                start = start - 1;
            }

            throw new IllegalArgumentException("Cannot find open bracket");
        }
    }
}
