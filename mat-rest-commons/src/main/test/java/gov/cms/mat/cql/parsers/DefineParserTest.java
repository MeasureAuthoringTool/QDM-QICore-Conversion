package gov.cms.mat.cql.parsers;

import org.junit.jupiter.api.Test;

class DefineParserTest {

    @Test
    void getDefines() {
        String data = "define \"Cessation Programs\":\n" +
                "  ( [\"Encounter, Performed\": \"Tobacco User with Cessation Intervention\"]\n" +
                "  \tunion [\"Procedure, Performed\": \"Tobacco User with Cessation Intervention\"]\n" +
                "  \tunion [\"Encounter, Performed\": \"Tobacco Use Cessation Counseling\"]\n" +
                "  \tunion [\"Procedure, Performed\": \"Tobacco Use Cessation Counseling\"] ) Counseling\n" +
                "  \twith \"Most Recent Encounter\" ENC\n" +
                "  \t\tsuch that ( Counseling.relevantPeriod starts 731 days or less after start of ENC.relevantPeriod\n" +
                "  \t\t\t\tor Counseling.relevantPeriod starts before start of ENC.relevantPeriod\n" +
                "  \t\t)";

    }


    define "Cessation Programs":
            ( ["Encounter, Performed": "Tobacco User with Cessation Intervention"]
              union ["Procedure, Performed": "Tobacco User with Cessation Intervention"]
              union ["Encounter, Performed": "Tobacco Use Cessation Counseling"]
              union ["Procedure, Performed": "Tobacco Use Cessation Counseling"] ) Counseling
              with "Most Recent Encounter" ENC
                such that ( Counseling.relevantPeriod starts 731 days or less after start of ENC.relevantPeriod
                or Counseling.relevantPeriod starts before start of ENC.relevantPeriod
            )
}

    define "Cessation Programs":
            // Contains UNIONS of different types. Not supported in FHIR.
            ( ["Encounter, Performed": "Tobacco User with Cessation Intervention"]
            union ["Procedure, Performed": "Tobacco User with Cessation Intervention"]
            union ["Encounter, Performed": "Tobacco Use Cessation Counseling"]
            union ["Procedure, Performed": "Tobacco Use Cessation Counseling"] ) Counseling
            with "Most Recent Encounter" ENC
            such that ( Counseling.relevantPeriod starts 731 days or less after start of ENC.relevantPeriod
            or Counseling.relevantPeriod starts before start of ENC.relevantPeriod
            )