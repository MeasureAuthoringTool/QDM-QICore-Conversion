package gov.cms.mat.fhir.services.rest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import mat.model.cql.CQLIncludeLibrary;
import mat.model.cql.CQLModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class CqlModelSerializationTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testSerializeDeserialize() throws JsonProcessingException {
        MatXmlController.MatCqlXmlReq origReq = new MatXmlController.MatCqlXmlReq();
        origReq.setCql("Very important CQL");
        CQLModel origPreviousModel = new CQLModel();
        origPreviousModel.setLibraryName("measure1");
        origPreviousModel.setVersionUsed("1.1");
        origPreviousModel.setUsingModel("FHIR");
        origPreviousModel.setUsingModelVersion("4.0.1");
        origPreviousModel.setLibraryComment("Library comment");

        CQLIncludeLibrary incLib1 = new CQLIncludeLibrary();
        incLib1.setCqlLibraryId("lib1Id");
        incLib1.setCqlLibraryName("lib1Name");

        origPreviousModel.getCqlIncludeLibrarys().add(incLib1);

        CQLModel inclLibModel = new CQLModel();
        inclLibModel.setLibraryName("libCqmModel");

        origPreviousModel.getIncludedLibrarys().put(incLib1, inclLibModel);
        origReq.setSourceModel(origPreviousModel);

        String serializedToXml = mapper.writeValueAsString(origReq);

        MatXmlController.MatCqlXmlReq deserializeReq = mapper.readValue(serializedToXml, MatXmlController.MatCqlXmlReq.class);
        assertNotNull(deserializeReq);
        assertNotNull(deserializeReq.getSourceModel());
        assertEquals("measure1", deserializeReq.getSourceModel().getLibraryName());
        assertEquals("1.1", deserializeReq.getSourceModel().getVersionUsed());
        assertEquals("FHIR", deserializeReq.getSourceModel().getUsingModel());
        assertEquals("4.0.1", deserializeReq.getSourceModel().getUsingModelVersion());
        assertEquals("Library comment", deserializeReq.getSourceModel().getLibraryComment());
        // Include libraries are not serialized
        assertEquals(0, deserializeReq.getSourceModel().getIncludedLibrarys().size());
        // Include libraries are not serialized
        assertEquals(0, deserializeReq.getSourceModel().getIncludedCQLLibXMLMap().size());

        assertEquals(1, deserializeReq.getSourceModel().getCqlIncludeLibrarys().size());
        CQLIncludeLibrary actualIncLib = deserializeReq.getSourceModel().getCqlIncludeLibrarys().get(0);
        assertEquals("lib1Name", actualIncLib.getCqlLibraryName());
        assertEquals("lib1Id", actualIncLib.getCqlLibraryId());
    }

}
