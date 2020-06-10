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

        CQLIncludeLibrary incLib1 = new CQLIncludeLibrary();
        incLib1.setCqlLibraryId("lib1Id");
        incLib1.setCqlLibraryName("lib1Name");

        origPreviousModel.getCqlIncludeLibrarys().add(incLib1);

        CQLModel inclLibModel = new CQLModel();
        inclLibModel.setLibraryName("libCqmModel");

        origPreviousModel.getIncludedLibrarys().put(incLib1, inclLibModel);
        origReq.setPreviousModel(origPreviousModel);

        String serializedToXml = mapper.writeValueAsString(origReq);

        MatXmlController.MatCqlXmlReq deserializeReq = mapper.readValue(serializedToXml, MatXmlController.MatCqlXmlReq.class);
        assertNotNull(deserializeReq);
        assertNotNull(deserializeReq.getPreviousModel());
        assertEquals("measure1", deserializeReq.getPreviousModel().getLibraryName());
        assertEquals("1.1", deserializeReq.getPreviousModel().getVersionUsed());
        assertEquals("FHIR", deserializeReq.getPreviousModel().getUsingModel());
        assertEquals("4.0.1", deserializeReq.getPreviousModel().getUsingModelVersion());
        // Include libraries are not serialized
        assertEquals(0, deserializeReq.getPreviousModel().getIncludedLibrarys().size());
        // Include libraries are not serialized
        assertEquals(0, deserializeReq.getPreviousModel().getIncludedCQLLibXMLMap().size());

        assertEquals(1, deserializeReq.getPreviousModel().getCqlIncludeLibrarys().size());
        CQLIncludeLibrary actualIncLib = deserializeReq.getPreviousModel().getCqlIncludeLibrarys().get(0);
        assertEquals("lib1Name", actualIncLib.getCqlLibraryName());
        assertEquals("lib1Id", actualIncLib.getCqlLibraryId());
    }

}
