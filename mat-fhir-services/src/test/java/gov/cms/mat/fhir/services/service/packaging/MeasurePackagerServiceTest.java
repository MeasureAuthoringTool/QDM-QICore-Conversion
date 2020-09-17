package gov.cms.mat.fhir.services.service.packaging;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.services.BundleTestHelper;
import gov.cms.mat.fhir.services.exceptions.FhirLibraryNotFoundException;
import gov.cms.mat.fhir.services.exceptions.FhirNotUniqueException;
import gov.cms.mat.fhir.services.exceptions.HapiResourceNotFoundException;
import gov.cms.mat.fhir.services.hapi.HapiFhirLinkProcessor;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.repository.CqlLibraryRepository;
import gov.cms.mat.fhir.services.service.packaging.dto.MeasurePackageFullHapi;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeasurePackagerServiceTest implements BundleTestHelper {

    private static final String ID = "id";
    private static final String URL = "http://iam.hapi.com";
    private static final String LIBRARY_URI = "/Library/1";
    private static final String LIBRARY_NAME = "SuperLib";
    @InjectMocks
    MeasurePackagerService measurePackagerService;
    @Mock
    private HapiFhirServer hapiFhirServer;
    @Mock
    private HapiFhirLinkProcessor hapiFhirLinkProcessor;
    @Mock
    private LibraryPackagerService libraryPackagerService;
    @Mock
    private CqlLibraryRepository cqlLibRepository;

    @Test
    void packageFullMeasureHappyPath() {
        Measure measure = new Measure();
        measure.setLibrary(List.of(new CanonicalType(LIBRARY_URI)));
        Bundle bundle = createBundle(URL, measure);
        when(hapiFhirServer.getMeasureBundle(ID)).thenReturn(bundle);


        CqlLibrary lib = new CqlLibrary();
        lib.setCqlName(LIBRARY_NAME);
        when(cqlLibRepository.getCqlLibraryByMeasureId("1")).thenReturn(lib);

        Library library = new Library();

        when(hapiFhirServer.fetchHapiLibraryByName(LIBRARY_NAME)).thenReturn(Optional.of(library));

        Bundle includeBundle = new Bundle();
        when(libraryPackagerService.buildIncludeBundle(library, ID)).thenReturn(includeBundle);

        MeasurePackageFullHapi measurePackageFullHapi = measurePackagerService.packageFull(ID);
        assertEquals(measure, measurePackageFullHapi.getMeasure());
        assertEquals(library, measurePackageFullHapi.getLibrary());
        assertEquals(includeBundle, measurePackageFullHapi.getIncludeBundle());

        verify(hapiFhirServer).getMeasureBundle(ID);
        verify(libraryPackagerService).buildIncludeBundle(library, ID);
    }

    @Test
    void packageFullMeasureLibraryNotFoundInHapi() {
        Measure measure = new Measure();
        measure.setLibrary(List.of(new CanonicalType(LIBRARY_URI)));
        Bundle bundle = createBundle(URL, measure);
        when(hapiFhirServer.getMeasureBundle(ID)).thenReturn(bundle);

        CqlLibrary lib = new CqlLibrary();
        lib.setCqlName("SuperLib");
        when(cqlLibRepository.getCqlLibraryByMeasureId("1")).thenReturn(lib);

        when(hapiFhirServer.fetchHapiLibraryByName(LIBRARY_NAME)).thenReturn(Optional.empty());

        assertThrows(HapiResourceNotFoundException.class, () -> {
            measurePackagerService.packageFull(ID);
        });

        verify(hapiFhirServer).getMeasureBundle(ID);

        verifyNoInteractions(libraryPackagerService);
    }


    @Test
    void packageFullMeasureFromHapiFoundLibraryNotFound() {
        Bundle bundle = createBundle(URL, new Measure());
        when(hapiFhirServer.getMeasureBundle(ID)).thenReturn(bundle);

        assertThrows(FhirLibraryNotFoundException.class, () -> {
            measurePackagerService.packageFull(ID);
        });

        verify(hapiFhirServer).getMeasureBundle(ID);
        verifyNoInteractions(hapiFhirLinkProcessor, libraryPackagerService);
    }

    @Test
    void packageFullMeasureFromHapiFoundTooManyLibrariesFound() {
        Measure measure = new Measure();
        measure.setLibrary(List.of(new CanonicalType("/Library/1"), new CanonicalType("/Library/2")));

        Bundle bundle = createBundle(URL, measure);
        when(hapiFhirServer.getMeasureBundle(ID)).thenReturn(bundle);

        assertThrows(FhirNotUniqueException.class, () -> {
            measurePackagerService.packageFull(ID);
        });

        verify(hapiFhirServer).getMeasureBundle(ID);
        verifyNoInteractions(hapiFhirLinkProcessor, libraryPackagerService);
    }


    @Test
    void packageFullMeasureFromHapiNotFound() {
        when(hapiFhirServer.getMeasureBundle(ID)).thenReturn(new Bundle());

        assertThrows(HapiResourceNotFoundException.class, () -> {
            measurePackagerService.packageFull(ID);
        });

        verify(hapiFhirServer).getMeasureBundle(ID);
        verifyNoInteractions(hapiFhirLinkProcessor, libraryPackagerService);
    }


    @Test
    void packageFullMeasureFromHapTooManyFound() {
        Bundle bundle = createBundle(URL, new Measure(), new Measure());
        when(hapiFhirServer.getMeasureBundle(ID)).thenReturn(bundle);


        assertThrows(FhirNotUniqueException.class, () -> {
            measurePackagerService.packageFull(ID);
        });

        verify(hapiFhirServer).getMeasureBundle(ID);
        verifyNoInteractions(hapiFhirLinkProcessor, libraryPackagerService);
    }

}