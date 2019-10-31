package gov.cms.mat.fhir.services.translate;

import ca.uhn.fhir.context.FhirContext;
import gov.cms.mat.fhir.services.components.mat.MatXmlConverter;
import gov.cms.mat.fhir.services.service.VsacService;
import mat.model.MatConcept;
import mat.model.MatConceptList;
import mat.model.MatValueSet;
import mat.model.VSACValueSetWrapper;
import mat.model.cql.CQLQualityDataModelWrapper;
import mat.model.cql.CQLQualityDataSetDTO;
import org.hl7.fhir.r4.model.ValueSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValueSetMapperTest {
    private static final String OID = "2.16.840.1.113762.1.4.1195.291";
    private static final String XML = "<xml>xml</xml>";

    FhirContext ctx = FhirContext.forR4();

    @Mock
    private VsacService vsacService;
    @Mock
    private MatXmlConverter matXmlConverter;
    @InjectMocks
    private ValueSetMapper valueSetMapper;

    @BeforeEach
    void setUp() {
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

        System.out.println(encoded);


        verify(matXmlConverter).toQualityData(XML);
        verify(vsacService).getData(OID);
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
        matValueSet.setVersion("N?A");
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