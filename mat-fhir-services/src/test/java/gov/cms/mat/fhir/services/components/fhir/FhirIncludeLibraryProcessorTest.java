package gov.cms.mat.fhir.services.components.fhir;

import gov.cms.mat.fhir.rest.dto.FhirIncludeLibraryResult;
import gov.cms.mat.fhir.services.ResourceFileUtil;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Library;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FhirIncludeLibraryProcessorTest implements ResourceFileUtil {
    @Mock
    private HapiFhirServer hapiFhirServer;

    @InjectMocks
    private FhirIncludeLibraryProcessor fhirIncludeLibraryProcessor;

    @Test
    void findIncludedFhirLibraries() {

        when(hapiFhirServer.fetchLibraryBundleByVersionAndName(anyString(), anyString()))
                .thenReturn(createBundle());


        String cql = getStringFromResource("/fhir_include_many.cql");

        FhirIncludeLibraryResult fhirIncludeLibraryResult = fhirIncludeLibraryProcessor.findIncludedFhirLibraries(cql);
        assertEquals("TJCOverall_FHIR4", fhirIncludeLibraryResult.getLibraryName());
        assertEquals("4.0.000", fhirIncludeLibraryResult.getLibraryVersion());
        // assertFalse();


    }

    private Bundle createBundle() {
        Bundle bundle = new Bundle();
        bundle.setEntry(createEntry());
        return bundle;
    }

    private List<Bundle.BundleEntryComponent> createEntry() {
        Bundle.BundleEntryComponent bundleEntryComponent = new Bundle.BundleEntryComponent();
        bundleEntryComponent.setResource(new Library());
        bundleEntryComponent.setLink(List.of(new Bundle.BundleLinkComponent().setUrl("http://your.goofy.com")));

        return List.of(bundleEntryComponent);
    }
}