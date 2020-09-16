package gov.cms.mat.fhir.services.service.packaging;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.services.exceptions.FhirLibraryNotFoundException;
import gov.cms.mat.fhir.services.exceptions.FhirNotUniqueException;
import gov.cms.mat.fhir.services.exceptions.HapiResourceNotFoundException;
import gov.cms.mat.fhir.services.hapi.HapiFhirLinkProcessor;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.repository.CqlLibraryRepository;
import gov.cms.mat.fhir.services.rest.support.FhirValidatorProcessor;
import gov.cms.mat.fhir.services.service.packaging.dto.MeasurePackageFullHapi;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MeasurePackagerService implements FhirValidatorProcessor {

    private static final String LIBRARY_CANONICAL_BASE = "http://ecqi.healthit.gov/ecqms/Library/";

    private static final String MEASURE_URL_TOKEN = "/Measure/";
    private final HapiFhirServer hapiFhirServer;
    private final HapiFhirLinkProcessor hapiFhirLinkProcessor;
    private final LibraryPackagerService libraryPackagerService;
    private final CqlLibraryRepository cqlLibRepository;

    public MeasurePackagerService(HapiFhirServer hapiFhirServer,
                                  HapiFhirLinkProcessor hapiFhirLinkProcessor,
                                  LibraryPackagerService libraryPackagerService,
                                  CqlLibraryRepository cqlLibRepository) {
        this.hapiFhirServer = hapiFhirServer;
        this.hapiFhirLinkProcessor = hapiFhirLinkProcessor;
        this.libraryPackagerService = libraryPackagerService;
        this.cqlLibRepository = cqlLibRepository;
    }

    public MeasurePackageFullHapi packageFull(String id) {
        Measure measure = fetchMeasureFromHapi(id);

        Library library = fetchLibraryFromHapi(measure);
        Bundle includedLibraryBundle = libraryPackagerService.buildIncludeBundle(library, id);

        return MeasurePackageFullHapi.builder()
                .measure(measure)
                .library(library)
                .includeBundle(includedLibraryBundle)
                .build();
    }

    private Library fetchLibraryFromHapi(Measure measure) {
        String name = getLibraryNameFromMeasure(measure);

        var optional = hapiFhirServer.fetchHapiLibraryByName(name);

        if (optional.isPresent()) {
            return optional.get();
        } else {
            throw new HapiResourceNotFoundException("Cannot find library with name: " + name);
        }
    }

    private String getLibraryNameFromMeasure(Measure measure) {
        if (CollectionUtils.isEmpty(measure.getLibrary())) {
            throw new FhirLibraryNotFoundException(measure.getId());
        }

        if (measure.getLibrary().size() > 1) {
            throw new FhirNotUniqueException("Measure Contains too many libs, id:" + measure.getId(),
                    measure.getLibrary().size());
        }

        //Can't use the url, it is just a canonical. Have to check the Mat DB.
        CqlLibrary lib = cqlLibRepository.getCqlLibraryByMeasureId(parseMeasureIdFromHapiUrl(measure.getId()));
        if (lib == null || CollectionUtils.isEmpty(measure.getLibrary())) {
            throw new FhirLibraryNotFoundException(measure.getId());
        }
        return lib.getCqlName();

    }

    private String parseMeasureIdFromHapiUrl(String url) {
        //http://hapi-fhir-jpaserver:6060/hapi-fhir-jpaserver/fhir/Measure/8a788f25739040ca0173904280310007/_history/31
        String result = null;
        int measureIndex = url.indexOf(MEASURE_URL_TOKEN);
        if (measureIndex != -1) {
            int nextSlash = url.indexOf('/', measureIndex + MEASURE_URL_TOKEN.length());
            if (nextSlash != -1) {
                result = url.substring(measureIndex + MEASURE_URL_TOKEN.length(), nextSlash);
            }
        } else {
            result = url;
        }


        return result;
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
