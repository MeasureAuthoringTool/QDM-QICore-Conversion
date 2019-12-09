package gov.cms.mat.cql_elm_translation.service;

import gov.cms.mat.cql_elm_translation.cql_translator.TranslationResource;
import gov.cms.mat.cql_elm_translation.data.RequestData;
import lombok.extern.slf4j.Slf4j;
import org.cqframework.cql.cql2elm.CqlTranslator;
import org.cqframework.cql.cql2elm.CqlTranslatorException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class CqlConversionService {
    public CqlTranslator processCqlData(RequestData requestData) {
        TranslationResource translationResource = new TranslationResource();
        CqlTranslator cqlTranslator =
                translationResource.buildTranslator(requestData.getCqlDataInputStream(), requestData.createMap());

        logErrors(cqlTranslator.getErrors());

        return cqlTranslator;
    }

    private void logErrors(List<CqlTranslatorException> exceptions) {
        exceptions
                .forEach(e -> log.warn("", e));
    }
}
