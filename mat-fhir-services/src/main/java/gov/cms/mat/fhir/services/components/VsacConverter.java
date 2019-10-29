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

@Component
@Slf4j
public class VsacConverter {

   public VSACValueSetWrapper toWrapper(String xml) {

        VSACValueSetWrapper details = null;

        if (StringUtils.isNotBlank(xml)) {
            log.info("xml To reterive RetrieveMultipleValueSetsResponse tag is not null ");
        }
        try {
            XMLMarshalUtil xmlMarshalUtil = new XMLMarshalUtil();
            details = (VSACValueSetWrapper) xmlMarshalUtil.convertXMLToObject("MultiValueSetMapping.xml",
                    xml,
                    VSACValueSetWrapper.class);

        } catch (MarshalException | ValidationException | MappingException | IOException e) {
            log.debug("Exception in convertXmltoValueSet:" + e);
            e.printStackTrace();
        }

        return details;
    }
}
