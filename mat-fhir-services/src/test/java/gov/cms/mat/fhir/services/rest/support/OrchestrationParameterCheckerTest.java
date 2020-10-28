package gov.cms.mat.fhir.services.rest.support;

import gov.cms.mat.fhir.rest.dto.ConversionType;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.exceptions.InvalidOrchestrationParametersException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class OrchestrationParameterCheckerTest {
    OrchestrationParameterCheckerJunit orchestrationParameterChecker = new OrchestrationParameterCheckerJunit();

    @Test
    void checkParameters_NoException() {
        orchestrationParameterChecker.checkParameters(XmlSource.SIMPLE, ConversionType.CONVERSION);
        orchestrationParameterChecker.checkParameters(XmlSource.SIMPLE, ConversionType.VALIDATION);
        orchestrationParameterChecker.checkParameters(XmlSource.MEASURE, ConversionType.VALIDATION);
    }

    @Test
    void checkParameters_ExceptionThrown() {
        Assertions.assertThrows(InvalidOrchestrationParametersException.class, () -> {
            orchestrationParameterChecker.checkParameters(XmlSource.MEASURE, ConversionType.CONVERSION);
        });
    }

    class OrchestrationParameterCheckerJunit implements OrchestrationParameterChecker {

    }
}