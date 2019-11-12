package gov.cms.mat.qdmqicore.conversion.controller;


import gov.cms.mat.qdmqicore.conversion.data.SearchData;
import gov.cms.mat.qdmqicore.conversion.dto.ConversionMapping;
import gov.cms.mat.qdmqicore.conversion.exceptions.ConversionMappingDataError;
import gov.cms.mat.qdmqicore.conversion.exceptions.ConversionMappingNotFound;
import gov.cms.mat.qdmqicore.conversion.service.ConversionDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/")
@Slf4j
public class ConversionDataController {
    private final ConversionDataService conversionDataService;

    public ConversionDataController(ConversionDataService conversionDataService) {
        this.conversionDataService = conversionDataService;
    }

    @GetMapping(path = "/all")
    public List<ConversionMapping> getAll() {
        return conversionDataService.getAll();
    }

    @GetMapping(path = "/find")
    public List<ConversionMapping> find(@RequestParam(required = false) String fhirResource,
                                        @RequestParam(required = false) String matAttributeName,
                                        @RequestParam(required = false) String fhirR4QiCoreMapping,
                                        @RequestParam(required = false) String fhirElement,
                                        @RequestParam(required = false) String fhirType,
                                        @RequestParam(required = false) String matDataTypeDescription) {
        SearchData searchData = SearchData.builder()
                .fhirResource(fhirResource)
                .matAttributeName(matAttributeName)
                .fhirR4QiCoreMapping(fhirR4QiCoreMapping)
                .fhirElement(fhirElement)
                .fhirType(fhirType)
                .matDataTypeDescription(matDataTypeDescription)
                .build();

        return conversionDataService.find(searchData);
    }

    @GetMapping(path = "/findOne")
    public ConversionMapping findOne(@RequestParam String matAttributeName,
                                     @RequestParam String matDataTypeDescription) {
        SearchData searchData = SearchData.builder()
                .matAttributeName(matAttributeName)
                .matDataTypeDescription(matDataTypeDescription)
                .build();

        List<ConversionMapping> conversionMappings = conversionDataService.find(searchData);

        if (CollectionUtils.isEmpty(conversionMappings)) {
            throw new ConversionMappingNotFound(matAttributeName, matDataTypeDescription);
        } else if (conversionMappings.size() > 1) {
            throw new ConversionMappingDataError(matAttributeName, matDataTypeDescription, conversionMappings.size());
        } else {
            return conversionMappings.get(0);
        }
    }
}
