package gov.cms.mat.cql_elm_translation.cql_translator;

import org.cqframework.cql.cql2elm.*;
import org.fhir.ucum.UcumEssenceService;
import org.fhir.ucum.UcumException;
import org.fhir.ucum.UcumService;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TranslationResource {
    public static final String ELM_JSON_TYPE = "application/elm+json";

    private static final MultivaluedMap<String, CqlTranslator.Options> PARAMS_TO_OPTIONS_MAP = new MultivaluedHashMap<String, CqlTranslator.Options>() {{
        putSingle("date-range-optimization", CqlTranslator.Options.EnableDateRangeOptimization);
        putSingle("annotations", CqlTranslator.Options.EnableAnnotations);
        putSingle("locators", CqlTranslator.Options.EnableLocators);
        putSingle("result-types", CqlTranslator.Options.EnableResultTypes);
        putSingle("detailed-errors", CqlTranslator.Options.EnableDetailedErrors);
        putSingle("disable-list-traversal", CqlTranslator.Options.DisableListTraversal);
        putSingle("disable-list-demotion", CqlTranslator.Options.DisableListDemotion);
        putSingle("disable-list-promotion", CqlTranslator.Options.DisableListPromotion);
        putSingle("enable-interval-demotion", CqlTranslator.Options.EnableIntervalDemotion);
        putSingle("enable-interval-promotion", CqlTranslator.Options.EnableIntervalPromotion);
        putSingle("disable-method-invocation", CqlTranslator.Options.DisableMethodInvocation);
        putSingle("require-from-keyword", CqlTranslator.Options.RequireFromKeyword);
        put("strict", Arrays.asList(
                CqlTranslator.Options.DisableListTraversal,
                CqlTranslator.Options.DisableListDemotion,
                CqlTranslator.Options.DisableListPromotion,
                CqlTranslator.Options.DisableMethodInvocation)
        );
        put("debug", Arrays.asList(
                CqlTranslator.Options.EnableAnnotations,
                CqlTranslator.Options.EnableLocators,
                CqlTranslator.Options.EnableResultTypes)
        );
        put("mat", Arrays.asList(
                CqlTranslator.Options.EnableAnnotations,
                CqlTranslator.Options.EnableLocators,
                CqlTranslator.Options.DisableListDemotion,
                CqlTranslator.Options.DisableListPromotion,
                CqlTranslator.Options.DisableMethodInvocation)
        );
    }};

    private final ModelManager modelManager;
    private final LibraryManager libraryManager;


    public TranslationResource() {
        this.modelManager = new ModelManager();
        this.libraryManager = new LibraryManager(modelManager);


    }

    public CqlTranslator buildTranslator(InputStream cqlStream, MultivaluedMap<String, String> params) {
        try {
            UcumService ucumService = null;
            LibraryBuilder.SignatureLevel signatureLevel = LibraryBuilder.SignatureLevel.None;
            List<CqlTranslator.Options> optionsList = new ArrayList<>();
            for (String key : params.keySet()) {
                if (PARAMS_TO_OPTIONS_MAP.containsKey(key) && Boolean.parseBoolean(params.getFirst(key))) {
                    optionsList.addAll(PARAMS_TO_OPTIONS_MAP.get(key));
                } else if (key.equals("validate-units") && Boolean.parseBoolean(params.getFirst(key))) {
                    try {
                        ucumService = new UcumEssenceService(UcumEssenceService.class.getResourceAsStream("/ucum-essence.xml"));
                    } catch (UcumException e) {
                        throw new TranslationFailureException("Cannot load UCUM service to validate units");
                    }
                } else if (key.equals("signatures")) {
                    signatureLevel = LibraryBuilder.SignatureLevel.valueOf(params.getFirst("signatures"));
                }
            }

            CqlTranslator.Options[] options = optionsList.toArray(new CqlTranslator.Options[optionsList.size()]);


            libraryManager.getLibrarySourceLoader().registerProvider(new FhirLibrarySourceProvider());
            return CqlTranslator.fromStream(cqlStream,
                    modelManager,
                    libraryManager,
                    ucumService,
                    CqlTranslatorException.ErrorSeverity.Info,
                    signatureLevel,
                    options);

        } catch (IOException e) {
            throw new TranslationFailureException("Unable to read request");
        }
    }
}
