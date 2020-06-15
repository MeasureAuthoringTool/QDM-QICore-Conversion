package gov.cms.mat.fhir.services.cql.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import lombok.extern.slf4j.Slf4j;
import mat.model.cql.CQLFunctions;
import mat.model.cql.CQLModel;
import mat.server.service.impl.XMLMarshalUtil;
import mat.server.util.XmlProcessor;

import static org.junit.Assert.assertEquals;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class ConversionCqlToMatXmlTest {
    private static final String CQL_TEST_RESOURCES_DIR = "/test-cql/";
    private XMLMarshalUtil xmlMarshalUtil = new XMLMarshalUtil();

    @Mock
    private MappingSpreadsheetService mappingService;

    @InjectMocks
    private AntlCqlParser parser;

    @InjectMocks
    private CqlToMatXml conversionCqlToMatXml;

    public String loadCqlResource(String cqlResource) throws IOException {
        log.info("This is to used a mock var so codacy will be happy. " + mappingService);
        try (InputStream i = ConversionCqlToMatXmlTest.class.getResourceAsStream(CQL_TEST_RESOURCES_DIR + cqlResource)) {
            return IOUtils.toString(i);
        }
    }

    public CQLModel loadMatXml(String fileName) throws Exception {
        String xml = loadCqlResource(fileName);
        XmlProcessor measureXMLProcessor = new XmlProcessor(xml);
        String cqlXmlFrag = measureXMLProcessor.getXmlByTagName("cqlLookUp");
        return (CQLModel) xmlMarshalUtil.convertXMLToObject("CQLModelMapping.xml", cqlXmlFrag, CQLModel.class);
    }

    @Test
    public void testConverted1() throws Exception {
//        when(codeSystemDAO.getAllCodeSystem()).thenReturn(new ArrayList<CodeSystemDTO>());

        conversionCqlToMatXml.setSourceModel(loadMatXml("convert-1-mat.xml"));
        parser.parse(loadCqlResource("convert-1.cql"), conversionCqlToMatXml);
        var destination = conversionCqlToMatXml.getDestinationModel();

        assertEquals("FHIR", destination.getUsingModel());
        assertEquals("4.0.1", destination.getUsingModelVersion());
        //TO DO: add more asserts when I get time.
        log.debug(conversionCqlToMatXml.toString());

        List<CQLFunctions> funs = destination.getCqlFunctions();
        CQLFunctions test1 = funs.stream().filter(f -> f.getName().equals("test1")).findFirst().get();
        assertEquals("test1", test1.getName());
        assertEquals("RolloutIntervalsOnce(RolloutIntervalsOnce(RolloutIntervalsOnce(RolloutIntervalsOnce(Intervals))))", test1.getLogic());
        assertEquals(1, test1.getArgumentList().size());
        assertEquals("Test_1", test1.getArgumentList().get(0).getArgumentName());
        assertEquals("Test \\\" 1", test1.getArgumentList().get(0).getQdmDataType());
        assertEquals("QDM Datatype", test1.getArgumentList().get(0).getArgumentType());

        CQLFunctions test2 = funs.stream().filter(f -> f.getName().equals("test2")).findFirst().get();
        assertEquals("test2", test2.getName());
        assertEquals("RolloutIntervalsOnce(RolloutIntervalsOnce(RolloutIntervalsOnce(RolloutIntervalsOnce(Intervals))))", test2.getLogic());
        assertEquals(1, test2.getArgumentList().size());
        assertEquals("Test_2", test2.getArgumentList().get(0).getArgumentName());
        assertEquals("Test , 2", test2.getArgumentList().get(0).getQdmDataType());
        assertEquals("QDM Datatype", test2.getArgumentList().get(0).getArgumentType());

        CQLFunctions test3 = funs.stream().filter(f -> f.getName().equals("test3")).findFirst().get();
        assertEquals("test3", test3.getName());
        assertEquals("RolloutIntervalsOnce(RolloutIntervalsOnce(RolloutIntervalsOnce(RolloutIntervalsOnce(Intervals))))", test3.getLogic());
        assertEquals(2, test3.getArgumentList().size());
        assertEquals("Test_3", test3.getArgumentList().get(0).getArgumentName());
        assertEquals("Test ,\\\" 3", test3.getArgumentList().get(0).getQdmDataType());
        assertEquals("QDM Datatype", test3.getArgumentList().get(0).getArgumentType());
        assertEquals("Value", test3.getArgumentList().get(1).getArgumentName());
        assertEquals(null, test3.getArgumentList().get(1).getQdmDataType());
        assertEquals("Integer", test3.getArgumentList().get(1).getArgumentType());

        CQLFunctions test4 = funs.stream().filter(f -> f.getName().equals("test4")).findFirst().get();
        assertEquals("test4", test4.getName());
        assertEquals("RolloutIntervalsOnce(RolloutIntervalsOnce(RolloutIntervalsOnce(RolloutIntervalsOnce(Intervals))))", test4.getLogic());
        assertEquals(3, test4.getArgumentList().size());
        assertEquals("Test_4", test4.getArgumentList().get(0).getArgumentName());
        assertEquals("Test ,\\\" 4", test4.getArgumentList().get(0).getQdmDataType());
        assertEquals("QDM Datatype", test4.getArgumentList().get(0).getArgumentType());
        assertEquals("b", test4.getArgumentList().get(1).getArgumentName());
        assertEquals("List<\"Medication, Order\">", test4.getArgumentList().get(1).getOtherType());
        assertEquals("Others", test4.getArgumentList().get(1).getArgumentType());
        assertEquals("QDM Datatype", test4.getArgumentList().get(2).getArgumentType());
        assertEquals("c", test4.getArgumentList().get(2).getArgumentName());
        assertEquals("C", test4.getArgumentList().get(2).getQdmDataType());


        CQLFunctions test5 = funs.stream().filter(f -> f.getName().equals("CalculateMME")).findFirst().get();
        assertEquals("CalculateMME", test5.getName());
        assertEquals(1, test5.getArgumentList().size());
        assertEquals("prescriptions", test5.getArgumentList().get(0).getArgumentName());
        assertEquals("Others", test5.getArgumentList().get(0).getArgumentType());
        assertEquals("List<Tuple {\n" +
                "  rxNormCode Code,\n" +
                "  doseQuantity Quantity,\n" +
                "  dosesPerDay Decimal\n" +
                "}>", test5.getArgumentList().get(0).getOtherType());
    }
}
