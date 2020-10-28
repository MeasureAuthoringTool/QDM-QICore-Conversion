package gov.cms.mat.cql.elements;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@Builder
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class ValueSetProperties extends BaseProperties {

    private static final String BASE_URL = "http://cts.nlm.nih.gov/fhir/ValueSet/";

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
    String urnOid;
    String line;

    boolean isSde;

    @Override
    public void setToFhir() {
        isSde = ArrayUtils.contains(SDE_OIDS, urnOid);
    }

    @Override
    public String createCql() {
        if (isSde) {
            return "";
        } else {
            String oid = parseOid();
            return line.replace(urnOid, BASE_URL + oid);
        }
    }

    private String parseOid() {
        return StringUtils.substringAfter(urnOid, "urn:oid:");
    }
}
