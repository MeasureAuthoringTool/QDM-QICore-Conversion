package gov.cms.mat.fhir.services.service;

import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.commons.model.MeasureExport;
import gov.cms.mat.fhir.commons.objects.TranslationOutcome;

public interface LibraryTranslationService {
    TranslationOutcome translate(Measure qdmMeasure, MeasureExport measureExport);
}
