package gov.cms.mat.fhir.services.rest.support;

import gov.cms.mat.fhir.rest.dto.ConversionType;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.exceptions.InvalidOrchestrationParametersException;

public interface OrchestrationParameterChecker {
    default void checkParameters(XmlSource xmlSource, ConversionType conversionType) {
        if (xmlSource == XmlSource.MEASURE && conversionType == ConversionType.CONVERSION) {
            throw new InvalidOrchestrationParametersException();
        }
    }
}
