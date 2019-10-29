package gov.cms.mat.fhir.services.components;

import lombok.extern.slf4j.Slf4j;
import mat.model.VSACValueSetWrapper;
import mat.server.service.impl.XMLMarshalUtil;
import org.apache.commons.lang3.StringUtils;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;

@Component
@Slf4j
public class VsacConverter {
    private final XMLMarshalUtil xmlMarshalUtil = new XMLMarshalUtil();

    public VSACValueSetWrapper toWrapper(String xml) {
        if (StringUtils.isBlank(xml)) {
            log.warn("Xml is blank");
            throw new UncheckedIOException(new IOException("Xml is blank"));
        } else {
            return convertXml(xml);
        }
    }

    private VSACValueSetWrapper convertXml(String xml) {
        try {
            return (VSACValueSetWrapper) xmlMarshalUtil.convertXMLToObject("MultiValueSetMapping.xml",
                    xml,
                    VSACValueSetWrapper.class);
        } catch (MarshalException | ValidationException | MappingException | IOException e) {
            throw new UncheckedIOException(new IOException(e));
        }
    }
}
