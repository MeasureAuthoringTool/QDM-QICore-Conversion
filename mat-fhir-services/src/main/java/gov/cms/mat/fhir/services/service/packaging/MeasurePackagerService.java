package gov.cms.mat.fhir.services.service.packaging;

import gov.cms.mat.fhir.services.exceptions.FhirLibraryNotFoundException;
import gov.cms.mat.fhir.services.exceptions.FhirNotUniqueException;
import gov.cms.mat.fhir.services.exceptions.HapiResourceNotFoundException;
import gov.cms.mat.fhir.services.hapi.HapiFhirLinkProcessor;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.rest.support.FhirValidatorProcessor;
import gov.cms.mat.fhir.services.service.packaging.dto.MeasurePackageFullHapi;
import gov.cms.mat.fhir.services.service.packaging.dto.MeasurePackageMinimumHapi;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MeasurePackagerService implements FhirValidatorProcessor {
    private final HapiFhirServer hapiFhirServer;
    private final HapiFhirLinkProcessor hapiFhirLinkProcessor;
    private final LibraryPackagerService libraryPackagerService;

    public MeasurePackagerService(HapiFhirServer hapiFhirServer,
                                  HapiFhirLinkProcessor hapiFhirLinkProcessor,
                                  LibraryPackagerService libraryPackagerService) {
        this.hapiFhirServer = hapiFhirServer;
        this.hapiFhirLinkProcessor = hapiFhirLinkProcessor;
        this.libraryPackagerService = libraryPackagerService;
    }

    public MeasurePackageMinimumHapi packageMinimum(String id) {
        Measure measure = fetchMeasureFromHapi(id);
        Library library = fetchLibraryFromHapi(measure);

        return MeasurePackageMinimumHapi.builder()
                .measure(measure)
                .library(library)
                .build();
    }

    public MeasurePackageFullHapi packageFull(String id) {
        Measure measure = fetchMeasureFromHapi(id);
        Library library = fetchLibraryFromHapi(measure);
        Bundle includedLibraryBundle = libraryPackagerService.buildIncludeBundle(library);

        return MeasurePackageFullHapi.builder()
                .measure(measure)
                .library(library)
                .includeBundle(includedLibraryBundle)
                .build();
    }

    private Library fetchLibraryFromHapi(Measure measure) {
        String url = getLibraryUrlFromMeasure(measure);

        var optional = hapiFhirLinkProcessor.fetchLibraryByUrl(url);

        if (optional.isPresent()) {
            return optional.get();
        } else {
            throw new HapiResourceNotFoundException(url, new Exception("Cannot find library"));
        }
    }

    private String getLibraryUrlFromMeasure(Measure measure) {
        if (CollectionUtils.isEmpty(measure.getLibrary())) {
            throw new FhirLibraryNotFoundException(measure.getId());
        }

        if (measure.getLibrary().size() > 1) {
            throw new FhirNotUniqueException("Measure Contains too many libs, id:" + measure.getId(), measure.getLibrary().size());
        }

        return measure.getLibrary().get(0).getValueAsString();
    }

    private Measure fetchMeasureFromHapi(String id) {
        Bundle bundle = hapiFhirServer.getMeasureBundle(id);

        if (bundle.hasEntry()) {
            if (bundle.getEntry().size() > 1) {
                throw new FhirNotUniqueException("Measure id:" + id, bundle.getEntry().size());
            } else {
                return (Measure) bundle.getEntry().get(0).getResource();
            }
        } else {
            throw new HapiResourceNotFoundException(id, "Measure");
        }
    }
}
