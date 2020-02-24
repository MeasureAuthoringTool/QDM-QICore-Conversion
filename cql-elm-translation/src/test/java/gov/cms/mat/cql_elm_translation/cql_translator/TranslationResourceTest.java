package gov.cms.mat.cql_elm_translation.cql_translator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TranslationResourceTest {

    @Test
    void buildTranslator_checkExceptionHandling() {
        TranslationResource translationResource = new TranslationResource(true);

        Assertions.assertThrows(TranslationFailureException.class, () -> {
            translationResource.buildTranslator(null, null);
        });
    }
}