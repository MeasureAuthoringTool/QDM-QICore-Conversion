package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.services.components.mongo.ConversionResultDto;
import gov.cms.mat.fhir.services.components.mongo.ConversionResultProcessorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/report")
@Slf4j
public class TranslationReportController {
    private final ConversionResultProcessorService conversionResultProcessorService;

    public TranslationReportController(ConversionResultProcessorService conversionResultProcessorService) {
        this.conversionResultProcessorService = conversionResultProcessorService;
    }

    @GetMapping(path = "/find")
    public ConversionResultDto findSearchData(String measureId) {
        return conversionResultProcessorService.process(measureId);
    }

    @GetMapping(path = "/findAll")
    public List<ConversionResultDto> findAll() {
        return conversionResultProcessorService.processAll();
    }
}
