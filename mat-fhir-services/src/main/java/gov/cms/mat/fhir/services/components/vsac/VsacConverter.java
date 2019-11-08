package gov.cms.mat.fhir.services.components.vsac;

import lombok.extern.slf4j.Slf4j;
import mat.model.VSACValueSetWrapper;
import mat.server.service.impl.XMLMarshalUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;

@Component
@Slf4j
public class VsacConverter {
    private final XMLMarshalUtil xmlMarshalUtil = new XMLMarshalUtil();

    public VSACValueSetWrapper toWrapper(String xml) {
        if (StringUtils.isBlank(xml)) {
            log.warn("Vsac Xml is blank");
            throw new UncheckedIOException(new IOException("Xml is blank xml: " +  xml));
        } else {
            return convertXml(xml);
        }
    }

    private VSACValueSetWrapper convertXml(String xml) {
        try {
            return (VSACValueSetWrapper) xmlMarshalUtil.convertXMLToObject("MultiValueSetMapping.xml",
                    xml,
                    VSACValueSetWrapper.class);
        } catch (Exception e) {
            throw new UncheckedIOException(new IOException(e));
        }
    }
}
