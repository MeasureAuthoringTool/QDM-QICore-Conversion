package gov.cms.mat.cql.elements;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.ArrayUtils;

@Builder
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class ValueSetProperties extends BaseProperties {

    //valueset "Ethnicity": 'urn:oid:2.16.840.1.114222.4.11.837'
    //valueset "ONC Administrative Sex": 'urn:oid:2.16.840.1.113762.1.4.1'
    // valueset "Payer": 'urn:oid:2.16.840.1.114222.4.11.3591'
    // valueset "Race": 'urn:oid:2.16.840.1.114222.4.11.836'

    private static final String[] SDE_OIDS = {
            "urn:oid:2.16.840.1.114222.4.11.837",
            "urn:oid:2.16.840.1.113762.1.4.1",
            "urn:oid:2.16.840.1.114222.4.11.3591",
            "urn:oid:2.16.840.1.114222.4.11.836"
    };

    String name;
    String oid;
    String line;

    boolean isSde = false;

    @Override
    public void setToFhir() {
        isSde = ArrayUtils.contains(SDE_OIDS, oid);
    }

    @Override
    public String createCql() {
        if (isSde) {
            return "";
        } else {
            return line;
        }
    }
}
