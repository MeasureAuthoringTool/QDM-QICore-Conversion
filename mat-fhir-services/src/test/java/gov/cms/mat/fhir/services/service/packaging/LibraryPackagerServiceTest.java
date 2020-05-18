package gov.cms.mat.fhir.services.service.packaging;

import gov.cms.mat.fhir.commons.objects.FhirResourceValidationError;
import gov.cms.mat.fhir.commons.objects.FhirResourceValidationResult;
import gov.cms.mat.fhir.rest.dto.FhirIncludeLibraryReferences;
import gov.cms.mat.fhir.rest.dto.FhirIncludeLibraryResult;
import gov.cms.mat.fhir.services.ResourceFileUtil;
import gov.cms.mat.fhir.services.components.fhir.FhirIncludeLibraryProcessor;
import gov.cms.mat.fhir.services.exceptions.FhirNotUniqueException;
import gov.cms.mat.fhir.services.exceptions.HapiResourceNotFoundException;
import gov.cms.mat.fhir.services.exceptions.HapiResourceValidationException;
import gov.cms.mat.fhir.services.exceptions.cql.LibraryAttachmentNotFoundException;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.rest.support.FhirValidatorService;
import gov.cms.mat.fhir.services.service.packaging.dto.LibraryPackageFullHapi;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Library;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LibraryPackagerServiceTest implements ResourceFileUtil, LibraryHelper {
    private final String ID = "id";
    String qiCorePatternCql;
    private Library qiCorePatternLibrary;
    private Library fhirHelpersLibrary;

    @Mock
    private HapiFhirServer hapiFhirServer;
    @Mock
    private FhirIncludeLibraryProcessor fhirIncludeLibraryProcessor;
    @Mock
    private FhirValidatorService fhirValidatorService;
    @InjectMocks
    private LibraryPackagerService libraryPackagerService;

    @BeforeEach
    void setUp() {
        qiCorePatternCql = getStringFromResource("/includes/QICorePatterns.cql");
        qiCorePatternLibrary = createLib(qiCorePatternCql);

        String fhirHelpersCql = getStringFromResource("/includes/FHIRHelpers.cql");
        fhirHelpersLibrary = createLib(fhirHelpersCql);
    }

    @Test
    void packageMinimumNotFoundInHapi() {
        when(hapiFhirServer.getLibraryBundle(ID)).thenReturn(new Bundle());

        Assertions.assertThrows(HapiResourceNotFoundException.class, () -> libraryPackagerService.packageMinimum(ID));

        verify(hapiFhirServer).getLibraryBundle(ID);
    }

    @Test
    void packageTooManyFoundInHapi() {
        buildAndSetUpBundle(qiCorePatternLibrary, fhirHelpersLibrary);

        Assertions.assertThrows(FhirNotUniqueException.class, () -> libraryPackagerService.packageMinimum(ID));

        verify(hapiFhirServer).getLibraryBundle(ID);
    }

    @Test
    void packageMinimumFoundInHapiValid() {
        buildAndSetUpBundle(qiCorePatternLibrary);

        when(fhirValidatorService.validate(qiCorePatternLibrary)).thenReturn(new FhirResourceValidationResult());

        Library library = libraryPackagerService.packageMinimum(ID);

        assertEquals(qiCorePatternLibrary, library);

        verify(hapiFhirServer).getLibraryBundle(ID);
        verify(fhirValidatorService).validate(qiCorePatternLibrary);
    }

    @Test
    void packageMinimumFoundInHapiInvalid() {
        buildAndSetUpBundle(qiCorePatternLibrary);

        FhirResourceValidationResult fhirResourceValidationResult = new FhirResourceValidationResult();
        fhirResourceValidationResult.getValidationErrorList()
                .add(new FhirResourceValidationError("ERROR", "local", "oops"));
        when(fhirValidatorService.validate(qiCorePatternLibrary)).thenReturn(fhirResourceValidationResult);

        Assertions.assertThrows(HapiResourceValidationException.class, () -> libraryPackagerService.packageMinimum(ID));

        verify(hapiFhirServer).getLibraryBundle(ID);
        verify(fhirValidatorService).validate(qiCorePatternLibrary);
    }

    @Test
    void packageFullIncludedLibrariesNoneFound() {
        buildAndSetUpBundle(qiCorePatternLibrary);
        when(fhirValidatorService.validate(qiCorePatternLibrary)).thenReturn(new FhirResourceValidationResult());
        when(fhirIncludeLibraryProcessor.findIncludedFhirLibraries(qiCorePatternCql))
                .thenReturn(new FhirIncludeLibraryResult());

        LibraryPackageFullHapi fullHapi = libraryPackagerService.packageFull(ID);
        assertEquals(qiCorePatternLibrary, fullHapi.getLibrary());
        assertFalse(fullHapi.getIncludeBundle().hasEntry());

        verify(hapiFhirServer).getLibraryBundle(ID);
        verify(fhirValidatorService).validate(qiCorePatternLibrary);
        verify(fhirIncludeLibraryProcessor).findIncludedFhirLibraries(qiCorePatternCql);
    }

    @Test
    void packageFullNoContentFound() {
        qiCorePatternLibrary.getContent().clear();
        buildAndSetUpBundle(qiCorePatternLibrary);
        when(fhirValidatorService.validate(qiCorePatternLibrary)).thenReturn(new FhirResourceValidationResult());

        Assertions.assertThrows(LibraryAttachmentNotFoundException.class, () -> libraryPackagerService.packageFull(ID));

        verify(hapiFhirServer).getLibraryBundle(ID);
        verify(fhirValidatorService).validate(qiCorePatternLibrary);
    }

    @Test
    void packageFullIncludedLibrariesFound() {
        buildAndSetUpBundle(qiCorePatternLibrary);
        when(fhirValidatorService.validate(qiCorePatternLibrary)).thenReturn(new FhirResourceValidationResult());


        FhirIncludeLibraryResult result = new FhirIncludeLibraryResult();
        FhirIncludeLibraryReferences referenceMissing = FhirIncludeLibraryReferences.builder()
                .name("missing")
                .version("1.0.000")
                .searchResult(false)
                .build();
        result.getLibraryReferences().add(referenceMissing);

        FhirIncludeLibraryReferences referenceFound = FhirIncludeLibraryReferences.builder()
                .name(fhirHelpersLibrary.getName())
                .version(fhirHelpersLibrary.getVersion())
                .searchResult(true)
                .library(fhirHelpersLibrary)
                .referenceEndpoint("http://stay.athome.com")
                .build();
        result.getLibraryReferences().add(referenceFound);

        when(fhirIncludeLibraryProcessor.findIncludedFhirLibraries(qiCorePatternCql))
                .thenReturn(result);

        LibraryPackageFullHapi fullHapi = libraryPackagerService.packageFull(ID);
        assertEquals(qiCorePatternLibrary, fullHapi.getLibrary());
        assertTrue(fullHapi.getIncludeBundle().hasEntry());

        assertEquals(1, fullHapi.getIncludeBundle().getEntry().size());
        assertEquals(fhirHelpersLibrary, fullHapi.getIncludeBundle().getEntry().get(0).getResource());

        verify(hapiFhirServer).getLibraryBundle(ID);
        verify(fhirValidatorService).validate(qiCorePatternLibrary);
        verify(fhirIncludeLibraryProcessor).findIncludedFhirLibraries(qiCorePatternCql);
    }


    private void buildAndSetUpBundle(Library... library) {
        Bundle bundle = new Bundle();

        Arrays.stream(library).forEach(lib -> bundle.addEntry()
                .setResource(lib));

        when(hapiFhirServer.getLibraryBundle(ID)).thenReturn(bundle);
    }
}