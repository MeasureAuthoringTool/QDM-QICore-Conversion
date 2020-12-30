package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.commons.model.HumanReadableArtifacts;
import gov.cms.mat.fhir.services.cql.CQLAntlrUtils;
import gov.cms.mat.fhir.services.cql.LibraryCqlVisitor;
import gov.cms.mat.fhir.services.cql.LibraryCqlVisitorFactory;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.repository.CqlLibraryRepository;
import gov.cms.mat.fhir.services.service.packaging.MeasurePackagerService;
import gov.cms.mat.fhir.services.service.packaging.dto.MeasurePackageFullData;
import gov.cms.mat.fhir.services.service.packaging.dto.MeasurePackageFullHapi;
import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeasurePackagerControllerTest {
    private static final String ID = "id";

    @Mock
    private MeasurePackagerService measurePackagerService;
    @Mock
    private HapiFhirServer hapiFhirServer;
    @Mock
    private CqlLibraryRepository cqlLibRepository;
    @Mock
    private LibraryCqlVisitorFactory cqlVisitorFactory;
    @Mock
    private CQLAntlrUtils cqlAntlrUtils;

    @InjectMocks
    private MeasurePackagerController measurePackagerController;

    @Test
    void packageFullJson() {
        Measure measure = new Measure();
        Library library = new Library();
        Bundle includeBundle = new Bundle();

        MeasurePackageFullHapi measurePackageFullHapi = MeasurePackageFullHapi.builder()
                .measure(measure)
                .library(library)
                .includeBundle(includeBundle)
                .build();

        when(measurePackagerService.packageFull(ID)).thenReturn(measurePackageFullHapi);

        when(hapiFhirServer.toJson(measure)).thenReturn("MEASURE");
        when(hapiFhirServer.toJson(library)).thenReturn("LIBRARY");
        when(hapiFhirServer.toJson(includeBundle)).thenReturn("INCLUDE_BUNDLE");

        MeasurePackageFullData measurePackageFullData = measurePackagerController.packageFullJson(ID);

        assertEquals("MEASURE", measurePackageFullData.getMeasure());
        assertEquals("LIBRARY", measurePackageFullData.getLibrary());
        assertEquals("INCLUDE_BUNDLE", measurePackageFullData.getIncludeBundle());

        verifyNoInteractions(cqlLibRepository, cqlVisitorFactory, cqlAntlrUtils);
    }

    @Test
    void getHumanReadableArtifactsCqlLibraryNotFound() {
        when(cqlLibRepository.getCqlLibraryByMeasureId(ID)).thenReturn(null);

        ResponseStatusException responseStatusException = Assertions.assertThrows(ResponseStatusException.class, () -> {
            measurePackagerController.getHumanReadableArtifacts(ID);
        });

        assertNotNull(responseStatusException.getReason());
        assertTrue(responseStatusException.getReason().startsWith("Could not find measure lib in MAT DB for measureId"));

        verifyNoInteractions(measurePackagerService, hapiFhirServer, cqlVisitorFactory, cqlAntlrUtils);
    }

    @Test
    void getHumanReadableArtifactsCqlLibraryFoundLibraryNotFound() {
        CqlLibrary cqlLibrary = new CqlLibrary();
        cqlLibrary.setId("cqlLibraryId");

        when(cqlLibRepository.getCqlLibraryByMeasureId(ID)).thenReturn(cqlLibrary);

        when(hapiFhirServer.fetchHapiLibrary(cqlLibrary.getId())).thenReturn(Optional.empty());

        ResponseStatusException responseStatusException = Assertions.assertThrows(ResponseStatusException.class, () -> {
            measurePackagerController.getHumanReadableArtifacts(ID);
        });

        assertNotNull(responseStatusException.getReason());
        assertTrue(responseStatusException.getReason().startsWith("Could not find Hapi Fhir Library "));

        verifyNoInteractions(measurePackagerService, cqlVisitorFactory, cqlAntlrUtils);
    }

    @Test
    void getHumanReadableArtifacts() {
        CqlLibrary cqlLibrary = new CqlLibrary();
        cqlLibrary.setId("cqlLibraryId");
        when(cqlLibRepository.getCqlLibraryByMeasureId(ID)).thenReturn(cqlLibrary);

        Library library = new Library();
        when(hapiFhirServer.fetchHapiLibrary(cqlLibrary.getId())).thenReturn(Optional.of(library));

        LibraryCqlVisitor libraryCqlVisitor = mock(LibraryCqlVisitor.class);

        HumanReadableArtifacts humanReadableArtifacts = new HumanReadableArtifacts();
        Pair<LibraryCqlVisitor, HumanReadableArtifacts> p = Pair.of(libraryCqlVisitor, humanReadableArtifacts);

        when(cqlAntlrUtils.getCql(library)).thenReturn("CQL");
        when(cqlVisitorFactory.visitAndCollateHumanReadable("CQL")).thenReturn(p);

        when(hapiFhirServer.fetchHapiLibrary("cqlLibraryId")).thenReturn(Optional.of(library));


        HumanReadableArtifacts result = measurePackagerController.getHumanReadableArtifacts(ID);
        assertEquals(humanReadableArtifacts, result);

        verifyNoInteractions(measurePackagerService);
        verifyNoMoreInteractions(hapiFhirServer, cqlVisitorFactory, cqlAntlrUtils);
    }
}