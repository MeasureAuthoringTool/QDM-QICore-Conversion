package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.service.packaging.MeasurePackagerService;
import gov.cms.mat.fhir.services.service.packaging.dto.MeasurePackageFullData;
import gov.cms.mat.fhir.services.service.packaging.dto.MeasurePackageFullHapi;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeasurePackagerControllerTest {
    private static final String ID = "id";

    @Mock
    private MeasurePackagerService measurePackagerService;
    @Mock
    private HapiFhirServer hapiFhirServer;

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
    }
}