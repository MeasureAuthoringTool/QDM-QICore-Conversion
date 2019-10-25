package gov.cms.mat.fhir.services.service;

import gov.cms.mat.fhir.commons.objects.TranslationOutcome;

public interface LibraryTranslationService {
    TranslationOutcome translateMeasureById(String id);
}
