package gov.cms.mat.cql_elm_translation.service;

import gov.cms.mat.cql.CqlTextParser;
import gov.cms.mat.cql.dto.CqlConversionPayload;
import gov.cms.mat.cql.elements.UsingProperties;
import gov.cms.mat.cql_elm_translation.cql_translator.MatLibrarySourceProvider;
import gov.cms.mat.cql_elm_translation.cql_translator.TranslationResource;
import gov.cms.mat.cql_elm_translation.data.RequestData;
import gov.cms.mat.cql_elm_translation.service.filters.AnnotationErrorFilter;
import gov.cms.mat.cql_elm_translation.service.filters.CqlTranslatorExceptionFilter;
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
        CqlTextParser cqlTextParser = new CqlTextParser(cql);

        MatLibrarySourceProvider.setUsing(cqlTextParser.getUsing());
        MatLibrarySourceProvider.setFhirServicesService(matFhirServices);
    }

    public CqlConversionPayload processCqlDataWithErrors(RequestData requestData) {
        CqlTranslator cqlTranslator = processCqlData(requestData);

        List<CqlTranslatorException> errors =
                processErrors(requestData.getCqlData(), requestData.isShowWarnings(), cqlTranslator.getExceptions());

        AnnotationErrorFilter annotationErrorFilter =
                new AnnotationErrorFilter(requestData.getCqlData(), requestData.isShowWarnings(), cqlTranslator.toJson());

        String processedJson = annotationErrorFilter.filter();

        String jsonWithErrors = attachErrorsToJson(errors, processedJson, cqlTranslator.getTranslatedLibrary());

        return CqlConversionPayload.builder()
                .json(jsonWithErrors)
                .xml(cqlTranslator.toXml())
                .build();
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
        CqlTextParser cqlTextParser = new CqlTextParser(new String(requestData.getCqlDataInputStream().readAllBytes()));
        UsingProperties usingProperties = cqlTextParser.getUsing();

        return new TranslationResource(usingProperties != null && usingProperties.isFhir())
                .buildTranslator(requestData.getCqlDataInputStream(), requestData.createMap());
    }

    private List<CqlTranslatorException> processErrors(String cqlData,
                                                       boolean showWarnings,
                                                       List<CqlTranslatorException> exceptions) {
        if (CollectionUtils.isEmpty(exceptions)) {
            log.debug("No CQL Errors found");
            return Collections.emptyList();
        } else {
            logErrors(exceptions);

            CqlTranslatorExceptionFilter cqlTranslatorExceptionFilter =
                    new CqlTranslatorExceptionFilter(cqlData, showWarnings, exceptions);

            return cqlTranslatorExceptionFilter.filter();
        }
    }

    private void logErrors(List<CqlTranslatorException> exceptions) {
        exceptions.forEach(e -> log.debug(formatMessage(e)));
    }

    private String formatMessage(CqlTranslatorException e) {
        return String.format(LOG_MESSAGE_TEMPLATE,
                e.getSeverity() != null ? e.getSeverity().name() : null,
                e.getMessage());
    }
}
