package gov.cms.mat.fhir.services.translate;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.commons.model.MeasureExport;
import gov.cms.mat.fhir.services.cql.CQLAntlrUtils;
import gov.cms.mat.fhir.services.cql.LibraryCqlVisitor;
import gov.cms.mat.fhir.services.cql.LibraryCqlVisitorFactory;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.repository.CqlLibraryRepository;
import gov.cms.mat.fhir.services.repository.MeasureExportRepository;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.utilities.xhtml.NodeType;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class TestLibraryTranslator {
    private static final String HAPI_BASE = "http://ecqi.healthit.gov/ecqms";
    private CQLAntlrUtils cqlAntlrUtils = new CQLAntlrUtils();

    @Mock
    private HapiFhirServer hapiFhirServer;
    @Mock
    private MeasureExportRepository measureExportRepo;
    @Mock
    private CqlLibraryRepository libRepo;
    @Mock
    private LibraryCqlVisitorFactory libCqlVisitorFactory;
    @Mock
    private LibraryCqlVisitor libCqlVisitor;

    private LibraryTranslator libTranslator;
    private FhirContext ctx = FhirContext.forR4();
    private IParser jsonParser;
    private String fhirHelpersJson = loadCqlResource("/libTranslator/library-FhirHelpers.json");
    private String matGlobalJson = loadCqlResource("/libTranslator/library-MATGlobalCommonFunctions.json");
    private String suppJson = loadCqlResource("/libTranslator/library-SupplementalDataElements.json");
    private String tjcJson = loadCqlResource("/libTranslator/library-TJCOverall.json");
    private String retrieveJson = loadCqlResource("/libTranslator/contains-retrieve.cql");

    @Before
    public void before() {
        jsonParser = ctx.newJsonParser();
        FhirContext ctx = FhirContext.forR4();
        libTranslator = new LibraryTranslator(hapiFhirServer, cqlAntlrUtils, libRepo, measureExportRepo, libCqlVisitorFactory);
        ReflectionTestUtils.setField(libTranslator,"matFhirBaseUrl","http://ecqi.healthit.gov/ecqms");
    }

    public String loadCqlResource(String cqlResource) {
        try (InputStream i = TestLibraryTranslator.class.getResourceAsStream(cqlResource)) {
            return IOUtils.toString(i);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    @Test
    public void testRetrieveAndRelatedArtifacts() throws IOException {
        Library fhirHelpers = jsonParser.parseResource(Library.class, fhirHelpersJson);
        Library matGlobalCommonFunctions = jsonParser.parseResource(Library.class, matGlobalJson);
        Library suppData = jsonParser.parseResource(Library.class, suppJson);
        Library tjc = jsonParser.parseResource(Library.class, tjcJson);

        CqlLibrary cqlLib = new CqlLibrary();
        cqlLib.setMeasureId("m12345");
        cqlLib.setCqlName("EXM104");

        MeasureExport mExport = new MeasureExport();
        mExport.setHumanReadable("<html><body>READABLE THAT IS HUAMN</body></html>".getBytes());

        when(hapiFhirServer.fetchHapiLibrary(eq("FHIRHelpers"), eq("4.0.001"))).
                thenReturn(Optional.of(fhirHelpers));
        when(hapiFhirServer.fetchHapiLibrary(eq("MATGlobalCommonFunctions_FHIR4"), eq("5.0.000"))).
                thenReturn(Optional.of(matGlobalCommonFunctions));
        when(hapiFhirServer.fetchHapiLibrary(eq("SupplementalDataElements_FHIR4"), eq("2.0.0"))).
                thenReturn(Optional.of(suppData));
        when(hapiFhirServer.fetchHapiLibrary(eq("TJCOverall_FHIR4"), eq("5.0.000"))).
                thenReturn(Optional.of(tjc));
        when(libRepo.getCqlLibraryById(eq("uuid"))).thenReturn(cqlLib);
        when(measureExportRepo.findByMeasureId(eq("m12345"))).thenReturn(Optional.of(mExport));

        when(libCqlVisitorFactory.visit(any())).thenReturn(libCqlVisitor);
        when(libCqlVisitor.getName()).thenReturn("EXM104");
        when(libCqlVisitor.getVersion()).thenReturn("9.1.000");


        Library lib = libTranslator.translateToFhir("uuid", retrieveJson,
                "<elm></elm>", "{elm:\"json\"}");

        IParser jsonParser = ctx.newJsonParser();

        CodeableConcept expectedType = new CodeableConcept()
                .setCoding(Collections.singletonList(new Coding(LibraryTranslator.SYSTEM_TYPE, LibraryTranslator.SYSTEM_CODE, null)));

        assertEquals("uuid", lib.getId());
        assertEquals("EXM104", lib.getName());
        assertEquals("9.1.000", lib.getVersion());
        assertEquals(Enumerations.PublicationStatus.ACTIVE, lib.getStatus());
        assertEquals(LibraryTranslator.CQL_CONTENT_TYPE, lib.getContent().get(0).getContentType());
        assertEquals(LibraryTranslator.XML_ELM_CONTENT_TYPE, lib.getContent().get(1).getContentType());
        assertEquals(LibraryTranslator.JSON_ELM_CONTENT_TYPE, lib.getContent().get(2).getContentType());
        assertArrayEquals(Base64.getEncoder().encode(retrieveJson.getBytes()), lib.getContent().get(0).getData());
        assertArrayEquals(Base64.getEncoder().encode("<elm></elm>".getBytes()), lib.getContent().get(1).getData());
        assertArrayEquals(Base64.getEncoder().encode("{elm:\"json\"}".getBytes()), lib.getContent().get(2).getData());
        assertEquals(HAPI_BASE + "/Library/" + lib.getName(), lib.getUrl());
        assertEquals(expectedType.getCoding().size(), lib.getType().getCoding().size());
        assertEquals(expectedType.getCoding().get(0).getSystem(), lib.getType().getCoding().get(0).getSystem());
        assertEquals(expectedType.getCoding().get(0).getCode(), lib.getType().getCoding().get(0).getCode());
        assertEquals(expectedType.getCoding().get(0).getDisplay(), lib.getType().getCoding().get(0).getDisplay());
        assertEquals(HAPI_BASE + "/Library/" + lib.getName(), lib.getUrl());

        assertEquals("generated", lib.getText().getStatusAsString());
        XhtmlNode expectedDiv = new XhtmlNode(NodeType.Element, "div");
        expectedDiv.setValueAsString("READABLE THAT IS HUAMN");
        assertEquals(expectedDiv.getValue(), StringUtils.remove(lib.getText().getDiv().getValue(), '\n'));
//        assertEquals(4,lib.getRelatedArtifact().size());
//        assertEquals(RelatedArtifact.RelatedArtifactType.DEPENDSON,lib.getRelatedArtifact().get(0).getType());
//        assertEquals(RelatedArtifact.RelatedArtifactType.DEPENDSON,lib.getRelatedArtifact().get(1).getType());
//        assertEquals(RelatedArtifact.RelatedArtifactType.DEPENDSON,lib.getRelatedArtifact().get(2).getType());
//        assertEquals(RelatedArtifact.RelatedArtifactType.DEPENDSON,lib.getRelatedArtifact().get(3).getType());
//        assertEquals(HAPI_BASE + "Library/FHIRHelpers-4-0-001",lib.getRelatedArtifact().get(0).getUrl());
//        assertEquals(HAPI_BASE + "Library/MATGlobalCommonFunctions-FHIR4-5-0-000",lib.getRelatedArtifact().get(1).getUrl());
//        assertEquals(HAPI_BASE + "Library/SupplementalDataElements-FHIR4-2-0-000",lib.getRelatedArtifact().get(2).getUrl());
//        assertEquals(HAPI_BASE + "Library/TJCOverall-FHIR4-5-0-000",lib.getRelatedArtifact().get(3).getUrl());
//        assertEquals(3,lib.getDataRequirement().size());
//        assertEquals("Condition",lib.getDataRequirement().get(0).getType());
//        //assertEquals(null,lib.getDataRequirement().get(0).getCodeFilter());
//        assertEquals("MedicationRequest",lib.getDataRequirement().get(1).getType());
//        assertEquals("MedicationRequest",lib.getDataRequirement().get(2).getType());
//        assertEquals("medication",lib.getDataRequirement().get(1).getCodeFilter().get(0).getPath());
//        assertEquals("medication",lib.getDataRequirement().get(2).getCodeFilter().get(0).getPath());
//        assertTrue(CollectionUtils.isEmpty(lib.getDataRequirement().get(0).getCodeFilter()));
//        assertEquals("'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.117.1.7.1.201'",
//                lib.getDataRequirement().get(1).getCodeFilter().get(0).getValueSet());
//        assertEquals("'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113762.1.4.1110.39'",
//                lib.getDataRequirement().get(2).getCodeFilter().get(0).getValueSet());
    }
}
