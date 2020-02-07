package gov.cms.mat.cql_elm_translation.service;

import gov.cms.mat.cql.CqlParser;
import gov.cms.mat.cql_elm_translation.cql_translator.MatLibrarySourceProvider;
import gov.cms.mat.cql_elm_translation.cql_translator.TranslationResource;
import gov.cms.mat.cql_elm_translation.data.RequestData;
import gov.cms.mat.cql_elm_translation.service.support.CqlExceptionErrorProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cqframework.cql.cql2elm.CqlTranslator;
import org.cqframework.cql.cql2elm.CqlTranslatorException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class CqlConversionService {
    private static final String LOG_MESSAGE_TEMPLATE = "ErrorSeverity: %s, Message: %s";
    private final FhirServicesService fhirServicesService;

    public CqlConversionService(FhirServicesService fhirServicesService) {
        this.fhirServicesService = fhirServicesService;
    }

    /* MatLibrarySourceProvider places version and service in thread local */
    public void setUpMatLibrarySourceProvider(String cql) {
        CqlParser cqlParser = new CqlParser(cql);
        CqlParser.LibraryProperties libraryProperties = cqlParser.getLibrary();

        MatLibrarySourceProvider.setQdmVersion(libraryProperties);
        MatLibrarySourceProvider.setFhirServicesService(fhirServicesService);
    }

    public String processCqlDataWithErrors(RequestData requestData) {
        CqlTranslator cqlTranslator = processCqlData(requestData);
        List<CqlTranslatorException> errors = processErrors(cqlTranslator.getExceptions());

        return attachErrorsToJson(errors, cqlTranslator.toJson());
    }

    private String attachErrorsToJson(List<CqlTranslatorException> errors, String json) {
        if (CollectionUtils.isEmpty(errors)) {
            return json;
        } else {
            return new CqlExceptionErrorProcessor(errors, json).process();
        }
    }

    public CqlTranslator processCqlData(RequestData requestData) {
        return new TranslationResource()
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
