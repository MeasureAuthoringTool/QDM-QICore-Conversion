package gov.cms.mat.fhir.services.components.fhir;

import gov.cms.mat.fhir.commons.objects.FhirResourceValidationResult;
import gov.cms.mat.fhir.rest.dto.FhirValidationResult;
import gov.cms.mat.fhir.services.components.reporting.ConversionReporter;
import gov.cms.mat.fhir.services.components.reporting.ConversionResult;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.exceptions.ValueSetValidationException;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.rest.support.FhirValidatorProcessor;
import gov.cms.mat.fhir.services.summary.FhirValueSetResourceValidationResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class ValueSetFhirValidationResults  {

    public FhirValueSetResourceValidationResult generate(List<ValueSet> valueSets,
                                                         XmlSource xmlSource,
                                                         String measureId) {
        if (CollectionUtils.isEmpty(valueSets)) {
            throw new ValueSetValidationException(measureId);
        } else {
            return generateResponse(xmlSource);
        }
    }

    public FhirValueSetResourceValidationResult generateResponse(XmlSource xmlSource) {
        FhirValueSetResourceValidationResult response = new FhirValueSetResourceValidationResult();

        ConversionResult conversionResult = ConversionReporter.getConversionResult();
        response.setValueSetConversionResults(conversionResult.getValueSetConversionResults());

        response.setXmlSource(xmlSource);

        return response;
    }
}
