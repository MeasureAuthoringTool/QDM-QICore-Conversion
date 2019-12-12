package gov.cms.mat.cql_elm_translation.service;

import gov.cms.mat.cql_elm_translation.cql_translator.TranslationResource;
import gov.cms.mat.cql_elm_translation.data.RequestData;
import gov.cms.mat.cql_elm_translation.exceptions.CqlConversionError;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cqframework.cql.cql2elm.CqlTranslator;
import org.cqframework.cql.cql2elm.CqlTranslatorException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CqlConversionService {
    private static final String TEMPLATE = "ErrorSeverity: %s, Message: %s";

    public CqlTranslator processCqlData(RequestData requestData) {

        TranslationResource translationResource = new TranslationResource();
        CqlTranslator cqlTranslator =
                translationResource.buildTranslator(requestData.getCqlDataInputStream(), requestData.createMap());

        List<String> errors = logErrors(cqlTranslator.getErrors());
        errors.addAll(logErrors(cqlTranslator.getWarnings()));

        if (CollectionUtils.isEmpty(errors)) {
            return cqlTranslator;
        } else {
            throw new CqlConversionError(errors);
        }
    }

    private List<String> logErrors(List<CqlTranslatorException> exceptions) {
        if (CollectionUtils.isEmpty(exceptions)) {
            return Collections.emptyList();
        } else {
            return exceptions.stream()
                    .map(this::process)
                    .collect(Collectors.toList());
        }
    }

    private String process(CqlTranslatorException e) {
        String message = String.format(TEMPLATE,
                e.getSeverity() != null ? e.getSeverity().name() : null,
                e.getMessage());

        log.debug(message, e);

        return message;
    }
}
