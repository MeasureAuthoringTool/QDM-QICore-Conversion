package gov.cms.mat.fhir.services.translate;

import gov.cms.mat.fhir.services.components.mat.MatXmlConverter;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.components.mongo.ConversionResultsService;
import gov.cms.mat.fhir.services.components.xml.MatXmlProcessor;
import gov.cms.mat.fhir.services.exceptions.ValueSetConversionException;
import gov.cms.mat.fhir.services.service.VsacService;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import lombok.extern.slf4j.Slf4j;
import mat.model.VSACValueSetWrapper;
import mat.model.cql.CQLQualityDataModelWrapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Slf4j
public class ValueSetVsacVerifier {
    private final VsacService vsacService;
    private final MatXmlProcessor matXmlProcessor;
    private final MatXmlConverter matXmlConverter;
    private final ConversionResultsService conversionResultsService;

    public ValueSetVsacVerifier(VsacService vsacService,
                                MatXmlProcessor matXmlProcessor,
                                MatXmlConverter matXmlConverter,
                                ConversionResultsService conversionResultsService) {
        this.vsacService = vsacService;
        this.matXmlProcessor = matXmlProcessor;
        this.matXmlConverter = matXmlConverter;
        this.conversionResultsService = conversionResultsService;
    }

    public boolean verify(OrchestrationProperties properties) {
        byte[] bytes = matXmlProcessor.getXml(properties.getMatMeasure(), properties.getXmlSource());

        CQLQualityDataModelWrapper wrapper = matXmlConverter.toQualityData(new String(bytes));

        if (wrapper == null || CollectionUtils.isEmpty(wrapper.getQualityDataDTO())) {
            throw new ValueSetConversionException("Cannot find any value sets in the xml for measure id: {}" +
                    properties.getMeasureId());
        }

        ConversionReporter.setInThreadLocal(properties.getMatMeasure().getId(), conversionResultsService);
        ConversionReporter.resetValueSetResults();

        AtomicBoolean atomicSuccessFlag = new AtomicBoolean(true);

        wrapper.getQualityDataDTO()
                .forEach(t -> verifyOidInVsac(t.getOid(), atomicSuccessFlag));

        return atomicSuccessFlag.get();
    }

    private void verifyOidInVsac(String oid, AtomicBoolean atomicSuccessFlag) {
        VSACValueSetWrapper vsacValueSetWrapper = vsacService.getData(oid);

        if (vsacValueSetWrapper == null) {
            log.debug("VsacService returned null for oid: {}", oid);
            atomicSuccessFlag.set(false);
            ConversionReporter.setValueSetInit(oid, "Not Found in VSAC");
        } else {
            log.debug("VsacService returned SUCCESS for oid: {}", oid);
            ConversionReporter.setValueSetInit(oid, "Found in VSAC");
        }
    }
}
