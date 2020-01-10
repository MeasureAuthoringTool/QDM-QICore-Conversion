package gov.cms.mat.cql_elm_translation.service;

import lombok.extern.slf4j.Slf4j;
import mat.model.cql.CQLModel;
import mat.server.CQLUtilityClass;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MatXmlConversionService {
    public String processCqlXml(String xml) {
        CQLModel cqlModel = CQLUtilityClass.getCQLModelFromXML(xml);
        return CQLUtilityClass.getCqlString(cqlModel, "");
    }
}
