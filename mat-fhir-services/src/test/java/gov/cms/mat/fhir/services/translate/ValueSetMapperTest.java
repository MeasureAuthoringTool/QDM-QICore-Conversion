package gov.cms.mat.fhir.services.translate;

import ca.uhn.fhir.context.FhirContext;
import gov.cms.mat.fhir.rest.dto.ConversionType;
import gov.cms.mat.fhir.services.components.mat.MatXmlConverter;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.components.mongo.ConversionResultsService;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.hapi.HapiFhirLinkProcessor;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.service.VsacService;
import mat.model.MatConcept;
import mat.model.MatConceptList;
import mat.model.MatValueSet;
import mat.model.VSACValueSetWrapper;
import mat.model.cql.CQLQualityDataModelWrapper;
import mat.model.cql.CQLQualityDataSetDTO;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.hl7.fhir.r4.model.Bundle.LINK_NEXT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

//import static org.hl7.fhir.r4.model.api.IBaseBundle.LINK_NEXT;

@ExtendWith(MockitoExtension.class)
class ValueSetMapperTest {
    private static final String OID = "2.16.840.1.113762.1.4.1195.291";
    private static final String XML = "<xml>xml</xml>";

    private final FhirContext ctx = FhirContext.forR4();

    @Mock
    private VsacService vsacService;
    @Mock
    private MatXmlConverter matXmlConverter;
    @Mock
    private HapiFhirServer hapiFhirServer;
    @Mock
    private HapiFhirLinkProcessor hapiFhirLinkProcessor;
    @Mock
    private ConversionResultsService conversionResultsService; // injected into  ConversionReporter

    @InjectMocks
    private ValueSetMapper valueSetMapper;

    @BeforeEach
    void setUp() {
        ConversionReporter.setInThreadLocal("measureId",
                "TEST",
                conversionResultsService,
                Instant.now(),
                ConversionType.CONVERSION,
                XmlSource.MEASURE,
                Boolean.TRUE);
    }

    @Test
    void count() {
        when(hapiFhirServer.count(ValueSet.class)).thenReturn(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, valueSetMapper.count());
        verify(hapiFhirServer).count(ValueSet.class);
    }

    @Test
    void deleteAll_LinkNextNull() {
        ValueSet valueSet = new ValueSet();
        Bundle bundle = createBundle(valueSet);

        when(hapiFhirServer.getAll(ValueSet.class)).thenReturn(bundle);

        assertEquals(1, valueSetMapper.deleteAll());

        verify(hapiFhirServer).delete(valueSet);
        verify(hapiFhirServer, never()).getNextPage(bundle);
    }

    @Test
    void deleteAll_LinkNextNotNullOneTime() {
        ValueSet valueSet = new ValueSet();

        Bundle bundleAll = createBundle(valueSet);
        bundleAll.setLink(Collections.singletonList(new Bundle.BundleLinkComponent().setUrl("http://elmer.fudd.com").setRelation(LINK_NEXT)));

        when(hapiFhirServer.getAll(ValueSet.class)).thenReturn(bundleAll);

        Bundle bundleNextPage = createBundle(valueSet);
        when(hapiFhirServer.getNextPage(bundleAll)).thenReturn(bundleNextPage);

        assertEquals(2, valueSetMapper.deleteAll());

        verify(hapiFhirServer, times(2)).delete(valueSet);
        verify(hapiFhirServer).getNextPage(bundleAll);
    }

    @Test
    void translateToFhir_matXmlConverterReturnsNull() {
        when(matXmlConverter.toQualityData(XML)).thenReturn(null);
        assertTrue(valueSetMapper.translateToFhir(XML).isEmpty());
        verify(matXmlConverter).toQualityData(XML);
    }

    @Test
    void translateToFhir_matXmlConverterReturnsEmptyList() {
        CQLQualityDataModelWrapper wrapper = new CQLQualityDataModelWrapper();
        when(matXmlConverter.toQualityData(XML)).thenReturn(wrapper);
        assertTrue(valueSetMapper.translateToFhir(XML).isEmpty());
        verify(matXmlConverter).toQualityData(XML);

        verifyNoInteractions(vsacService);
    }

    @Test
    void translateToFhir_VsacServiceReturnsNull() {
        CQLQualityDataModelWrapper wrapper = new CQLQualityDataModelWrapper();
        wrapper.setQualityDataDTO(Collections.singletonList(create()));

        when(matXmlConverter.toQualityData(XML)).thenReturn(wrapper);
        when(vsacService.getData(OID)).thenReturn(null);

        assertTrue(valueSetMapper.translateToFhir(XML).isEmpty());

        verify(matXmlConverter).toQualityData(XML);
        verify(vsacService).getData(OID);
    }


    @Test
    void translateToFhir_OK() {
        CQLQualityDataModelWrapper wrapper = new CQLQualityDataModelWrapper();
        wrapper.setQualityDataDTO(Collections.singletonList(create()));

        VSACValueSetWrapper vsacValueSetWrapper = new VSACValueSetWrapper();
        vsacValueSetWrapper.setValueSetList(createValueSetList());
        when(vsacService.getData(OID)).thenReturn(vsacValueSetWrapper);

        when(matXmlConverter.toQualityData(XML)).thenReturn(wrapper);

        List<ValueSet> valueSets = valueSetMapper.translateToFhir(XML);
        assertEquals(1, valueSets.size());

        String encoded = ctx.newXmlParser().setPrettyPrint(true)
                .encodeResourceToString(valueSets.get(0));

        assertTrue(encoded.contains("ValueSet"));
        verify(matXmlConverter).toQualityData(XML);
        verify(vsacService).getData(OID);
    }

    @Test
    void translateToFhir_EmptyBundle() {

        CQLQualityDataModelWrapper wrapper = new CQLQualityDataModelWrapper();
        wrapper.setQualityDataDTO(Collections.singletonList(create()));

        VSACValueSetWrapper vsacValueSetWrapper = new VSACValueSetWrapper();
        vsacValueSetWrapper.setValueSetList(createValueSetList());

        when(matXmlConverter.toQualityData(XML)).thenReturn(wrapper);

        valueSetMapper.translateToFhir(XML);

    }

    /* Need all this data set to get past bundle.isEmpty() */
    private Bundle createBundle(Resource value) {
        Bundle bundle = new Bundle();
        bundle.setIdentifier(new Identifier().setValue("value").setSystem("system"));
        bundle.setType(Bundle.BundleType.TRANSACTION);
        bundle.setTimestamp(new Date());
        bundle.setTotal(1);
        bundle.setLink(Collections.singletonList(new Bundle.BundleLinkComponent().setUrl("http://mickey.mouse.com")));
        bundle.setSignature(new Signature());

        Bundle.BundleEntryComponent bundleEntryComponent = new Bundle.BundleEntryComponent();
        bundleEntryComponent.setResource(value);
        bundleEntryComponent.setLink(Collections.singletonList(new Bundle.BundleLinkComponent().setUrl("http://donald.duck.com")));
        bundleEntryComponent.setFullUrl("http://donald.duck.com" + "/login");
        bundleEntryComponent.setSearch(new Bundle.BundleEntrySearchComponent());
        bundleEntryComponent.setRequest(new Bundle.BundleEntryRequestComponent());
        bundleEntryComponent.setResponse(new Bundle.BundleEntryResponseComponent());

        bundle.addEntry(bundleEntryComponent);

        return bundle;
    }

    private ArrayList<MatValueSet> createValueSetList() {
        ArrayList<MatValueSet> list = new ArrayList<>();

        list.add(createMatValueSet());

        return list;
    }

    private MatValueSet createMatValueSet() {
        MatValueSet matValueSet = new MatValueSet();
        matValueSet.setID("2.16.840.1.113762.1.4.1195.291");
        matValueSet.setDisplayName("HEDIS Bone Mineral Density Tests Value Set");
        matValueSet.setVersion("1.3");
        matValueSet.setSource("New Wakanda Innovation Institute");
        matValueSet.setDefinition("(2.16.840.1.113762.1.4.1195.288:Bone Mineral Density Tests\n" +
                "            CPT),(2.16.840.1.113762.1.4.1195.289:Bone Mineral Density Tests HCPCS),(2.16.840.1.113762.1.4.1195.290:Bone\n" +
                "            Mineral Density Tests ICD10PCS)");
        matValueSet.setType("Grouping");
        matValueSet.setBinding("Dynamic");
        matValueSet.setRevisionDate("2019-10-28");

        MatConceptList matConceptList = new MatConceptList();
        matConceptList.setConceptList(Collections.singletonList(createMatConcept()));
        matValueSet.setConceptList(matConceptList);

        return matValueSet;
    }

    private MatConcept createMatConcept() {
        MatConcept matConcept = new MatConcept();
        matConcept.setCode("76977");
        matConcept.setCodeSystem("2.16.840.1.113883.6.12");
        matConcept.setCodeSystemName("CPT");
        matConcept.setCodeSystemVersion("2019");
        matConcept.setDisplayName("Ultrasound bone density measurement and interpretation, peripheral site(s), any method");

        return matConcept;
    }

    private CQLQualityDataSetDTO create() {
        CQLQualityDataSetDTO cqlQualityDataSetDTO = new CQLQualityDataSetDTO();
        cqlQualityDataSetDTO.setOid(OID);

        return cqlQualityDataSetDTO;
    }
}