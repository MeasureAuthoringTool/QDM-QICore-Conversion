package gov.cms.mat.cql_elm_translation.controllers;


import gov.cms.mat.cql_elm_translation.service.CqlConversionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/cql/marshaller")
@Tag(name = "XmlMarshal-Controller", description = "API for converting stuff - TODO.")
@Slf4j
public class CqlXmlMarshalController {
    private final CqlConversionService cqlConversionService;

    public CqlXmlMarshalController(CqlConversionService cqlConversionService) {
        this.cqlConversionService = cqlConversionService;
    }

    @PostMapping(consumes = "text/plain", produces = "text/plain")
    public String convertXmlToCql(@RequestBody String xml) {
        return cqlConversionService.processCqlXml(xml);
    }
}
