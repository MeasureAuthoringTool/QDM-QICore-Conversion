package gov.cms.mat.cql.elements;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ValueSetPropertiesTest {
    @Test
    void setToFhir_ConvertUrnToLink() {

        ValueSetProperties valueSetProperties = ValueSetProperties.builder()
                .line("valueset \"Aortic Dissection\": 'urn:oid:2.16.840.1.113883.17.4077.3.1026'")
                .name("Aortic Dissection")
                .urnOid("urn:oid:2.16.840.1.113883.17.4077.3.1026")
                .build();

        valueSetProperties.setToFhir();
        String cql = valueSetProperties.createCql();
        assertEquals("valueset \"Aortic Dissection\": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.17.4077.3.1026'",
                cql);
    }

    @Test
    void setToFhir_SkipSDE() {
        ValueSetProperties valueSetProperties = ValueSetProperties.builder()
                .line("valueset \"Aortic Dissection\": 'urn:oid:2.16.840.1.114222.4.11.837'")
                .name("Aortic Dissection")
                .urnOid("urn:oid:2.16.840.1.114222.4.11.837")
                .build();

        valueSetProperties.setToFhir();
        String cql = valueSetProperties.createCql();
        assertEquals("", cql);
    }
}