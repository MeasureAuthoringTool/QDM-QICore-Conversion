package gov.cms.mat.cql_elm_translation.service;

import gov.cms.mat.cql.CqlParser;
import gov.cms.mat.cql.elements.UsingProperties;
import gov.cms.mat.cql_elm_translation.cql_translator.MatLibrarySourceProvider;
import gov.cms.mat.cql_elm_translation.cql_translator.TranslationResource;
import gov.cms.mat.cql_elm_translation.data.RequestData;
import gov.cms.mat.cql_elm_translation.service.support.CqlExceptionErrorProcessor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cqframework.cql.cql2elm.CqlTranslator;
import org.cqframework.cql.cql2elm.CqlTranslatorException;
import org.cqframework.cql.cql2elm.model.TranslatedLibrary;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class CqlConversionService {
    private static final String LOG_MESSAGE_TEMPLATE = "ErrorSeverity: %s, Message: %s";
    private final MatFhirServices matFhirServices;

    public CqlConversionService(MatFhirServices matFhirServices) {
        this.matFhirServices = matFhirServices;
    }

    /* MatLibrarySourceProvider places version and service in thread local */
    public void setUpMatLibrarySourceProvider(String cql) {
        CqlParser cqlParser = new CqlParser(cql);

        MatLibrarySourceProvider.setQdmVersion(cqlParser.getUsing());
        MatLibrarySourceProvider.setFhirServicesService(matFhirServices);
    }

    public String processCqlDataWithErrors(RequestData requestData) {
        CqlTranslator cqlTranslator = processCqlData(requestData);
        List<CqlTranslatorException> errors = processErrors(cqlTranslator.getExceptions());

        return attachErrorsToJson(errors, cqlTranslator.toJson(), cqlTranslator.getTranslatedLibrary());
    }

    private String attachErrorsToJson(List<CqlTranslatorException> errors, String json, TranslatedLibrary translatedLibrary) {
        if (CollectionUtils.isEmpty(errors) || translatedLibrary == null || translatedLibrary.getLibrary() == null) {
            return json;
        } else {
            return new CqlExceptionErrorProcessor(errors, json, translatedLibrary.getLibrary()).process();
        }
    }

    @SneakyThrows
    public CqlTranslator processCqlData(RequestData requestData) {
        CqlParser cqlParser = new CqlParser(new String(requestData.getCqlDataInputStream().readAllBytes()));
        UsingProperties usingProperties = cqlParser.getUsing();

        return new TranslationResource(usingProperties.isFhir())
                .buildTranslator(requestData.getCqlDataInputStream(), requestData.createMap());
    }

    private List<CqlTranslatorException> processErrors(List<CqlTranslatorException> exceptions) {
        if (CollectionUtils.isEmpty(exceptions)) {
            log.debug("No CQL Errors found");
            return Collections.emptyList();
        } else {
            logErrors(exceptions);
            return exceptions;
        }
    }

    private void logErrors(List<CqlTranslatorException> exceptions) {
        exceptions.forEach(e -> log.warn(formatMessage(e)));
    }

    private String formatMessage(CqlTranslatorException e) {
        return String.format(LOG_MESSAGE_TEMPLATE,
                e.getSeverity() != null ? e.getSeverity().name() : null,
                e.getMessage());
    }
}
