package gov.cms.mat.cql_elm_translation.service;

import gov.cms.mat.cql_elm_translation.cql_translator.MatLibrarySourceParser;
import gov.cms.mat.cql_elm_translation.cql_translator.MatLibrarySourceProvider;
import gov.cms.mat.cql_elm_translation.cql_translator.TranslationResource;
import gov.cms.mat.cql_elm_translation.data.RequestData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.cqframework.cql.cql2elm.CqlTranslator;
import org.cqframework.cql.cql2elm.CqlTranslatorException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class CqlConversionService {
    private static final String LOG_MESSAGE_TEMPLATE = "ErrorSeverity: %s, Message: %s";
    private final FhirServicesService fhirServicesService;

    public CqlConversionService(FhirServicesService fhirServicesService) {
        this.fhirServicesService = fhirServicesService;
    }

    public void processCqlLibraries(String cqlData) {

        MatLibrarySourceParser parser = new MatLibrarySourceParser(cqlData);
        MatLibrarySourceParser.ParseResults results = parser.parse();

        MatLibrarySourceProvider.setQdmVersion(results.getQdmVersion());
        MatLibrarySourceProvider.setFhirServicesService(fhirServicesService);


//        results.getLibraries()
//                .stream()
//                .filter(l -> isRequired(l, results.getQdmVersion()))
//                .forEach(l -> getFromRest(l, results.getQdmVersion()));
    }

    private void getFromRest(MatLibrarySourceParser.LibraryItem l, String qdmVersion) {

        String cql = fhirServicesService.getCql(l.getName(), l.getLibraryVersion(), qdmVersion);

        if (StringUtils.isEmpty(cql)) {
            log.debug("Did not find any cql");
        } else {
            MatLibrarySourceProvider.addLibraryInMap(l.getName(), qdmVersion, l.getLibraryVersion(), cql);
        }
    }

    private boolean isRequired(MatLibrarySourceParser.LibraryItem item, String qdmVersion) {
        return !MatLibrarySourceProvider.isLibraryInMap(item.getName(), qdmVersion, item.getLibraryVersion());
    }


    public CqlTranslator processCqlData(RequestData requestData) {
        TranslationResource translationResource = new TranslationResource();
        CqlTranslator cqlTranslator =
                translationResource.buildTranslator(requestData.getCqlDataInputStream(), requestData.createMap());

        logErrors(cqlTranslator.getErrors());

        return cqlTranslator;
    }

    private void logErrors(List<CqlTranslatorException> exceptions) {
        if (CollectionUtils.isEmpty(exceptions)) {
            log.trace("No Errors found");
        } else {
            exceptions
                    .forEach(e -> log.warn(formatMessage(e)));
        }
    }

    private String formatMessage(CqlTranslatorException e) {
        return String.format(LOG_MESSAGE_TEMPLATE,
                e.getSeverity() != null ? e.getSeverity().name() : null,
                e.getMessage());
    }
}
