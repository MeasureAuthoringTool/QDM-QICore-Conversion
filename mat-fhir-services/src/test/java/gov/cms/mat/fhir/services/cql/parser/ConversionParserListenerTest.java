package gov.cms.mat.fhir.services.cql.parser;

import gov.cms.mat.fhir.services.ResourceFileUtil;
import gov.cms.mat.fhir.services.components.conversion.ConversionDataComponent;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled // Integration test with mapping services
class ConversionParserListenerTest implements ResourceFileUtil {

    String expectedResults[] = {
            "[\"Encounter\": \"Inpatient\"] Encounter\n" +
                    "    where Encounter.period during \"Measurement Period\"",

            "[\"Encounter\": \"Inpatient\"] Encounter\n" +
                    "    with [\"Observation\": \"Streptococcus Test\"] LabTest\n" +
                    "      such that LabTest.issued during Encounter.period",
            "[\"Encounter\": \"Inpatient\"] Encounter\n" +
                    "    without [\"Observation\": \"Streptococcus Test\"] LabTest\n" +
                    "      such that LabTest.issued during Encounter.period",
            "[\"Encounter\": \"Inpatient\"] Encounter\n" +
                    "    return { relevantPeriod: Encounter.period }",
            "[\"Encounter\": \"Inpatient\"] Encounter\n" +
                    "    sort by start of period",
            "[\"Encounter\": \"Inpatient\"] Encounter\n" +
                    "    with [\"Procedure\": \"Some Valueset\"] Device\n" +
                    "      such that (Device.performed as Period).start during Encounter.period",
            "[\"Encounter\": \"Inpatient\"] Encounter\n" +
                    "   with [\"Observation\": \"Some Valueset\"] Study\n" +
                    "     such that (Study.effective as Period).end during Encounter.period",
            "[\"Encounter\": \"Inpatient\"] Encounter\n" +
                    "    with [\"Procedure\": \"Some Valueset\"] Proc\n" +
                    "      such that Proc.authorDatetime during Encounter.period //No FHIR mapping for 'authorDatetime'\n",
            "[\"Patient Characteristic Expired\": \"Some Valueset\"] CharExp //Patient Characteristic Expired' requires logical mapping\n" +
                    "    return { relevantPeriod: CharExp.cause } //FHIR requires additional information not in logic for 'cause'",

            "[\"ServiceRequest\": \"Some Valueset\"] Assess\n" +
                    "    where Assess.status = 'completed' and Assess.doNotPerform is true and\n" +
                    "       Assess.authoredOn during \"Measurement Period\"",
            "[\"Assessment, Not Ordered\": \"Some Valueset\"] Assessment //Unable to convert negation without a where clause.\n" +
                    "      with [\"Laboratory Test, Performed\": \"Streptococcus Test\"] LabTest\n" +
                    "        such that LabTest.resultDateTime during Assessment.relevantPeriod",
            "( [\"Encounter\": \"Tobacco User with Cessation Intervention\"]\n" +
                    "    union [\"Procedure\": \"Tobacco User with Cessation Intervention\"]\n" +
                    "    union [\"Encounter\": \"Tobacco Use Cessation Counseling\"]\n" +
                    "    union [\"Procedure\": \"Tobacco Use Cessation Counseling\"] ) Counseling\n" +
                    "      with \"Most Recent Encounter\" ENC\n" +
                    "        such that ( Counseling.relevantPeriod starts 731 days or less after start of ENC.relevantPeriod //Unable to convert relevantPeriod for mixed types.\n" +
                    "            or Counseling.relevantPeriod starts before start of ENC.relevantPeriod //Unable to convert relevantPeriod for mixed types.\n" +
                    "    )",
            "define function \"Test 17\" (foo Boolean):\n" +
                    "  [\"Observation\"]",
            "define \"Test 18\":\n" +
                    "  [\"Encounter\"]"
    };

    String noChanges = "\"A define that does a retrieve\" Encounter\n" +
            "     where Encounter.relevantPeriod during \"Measurement Period\"";

    @Test
    void parse() {
        String cql = getStringFromResource("/conversion_antlr.cql");

        RestTemplate restTemplate = new RestTemplate();
        MappingSpreadsheetService mappingSpreadsheetService = new MappingSpreadsheetService(restTemplate);

        ReflectionTestUtils.setField(mappingSpreadsheetService, "conversionUrl", "http://localhost:9090");
        ConversionDataComponent conversionDataComponent = new ConversionDataComponent(mappingSpreadsheetService);

        ConversionParserListener parserListener = new ConversionParserListener(conversionDataComponent);

        String results = parserListener.convert(cql);

        System.out.println(results);

        Arrays.stream(expectedResults)
                //.peek(e -> System.out.println("Expected->" + e))
                .forEach(expected ->
                        assertTrue(results.contains(expected)));
        assertTrue(results.contains(noChanges));
    }

}