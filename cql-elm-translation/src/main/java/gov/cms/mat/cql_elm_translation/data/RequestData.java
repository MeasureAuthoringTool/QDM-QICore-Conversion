package gov.cms.mat.cql_elm_translation.data;

import lombok.Builder;
import org.cqframework.cql.cql2elm.LibraryBuilder;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Builder
public class RequestData {
    String cqlData;
    LibraryBuilder.SignatureLevel signatures;
    Boolean annotations;
    Boolean locators;
    Boolean disableListDemotion;
    Boolean disableListPromotion;
    Boolean disableMethodInvocation;
    Boolean validateUnits;

    public InputStream getCqlDataInputStream() {
        return new ByteArrayInputStream(cqlData.getBytes());
    }

    public MultivaluedMap<String, String> createMap() {
        MultivaluedMap<String, String> map = new MultivaluedHashMap<>();

        map.add("annotations", annotations.toString());
        map.add("locators", locators.toString());
        map.add("disable-list-demotion", disableListDemotion.toString());
        map.add("disable-list-promotion", disableListPromotion.toString());
        map.add("disable-method-invocation", disableMethodInvocation.toString());
        map.add("validate-units", validateUnits.toString());

        map.add("detailed-errors", Boolean.TRUE.toString());


        if (signatures != null) {
            map.add("signatures", signatures.name());
        }

        return map;
    }
}
