package mat.server;

import mat.model.cql.CQLModel;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;

public class CQLUtilityClassTest {
    @Test
    public void getCQLModelFromXML_EnsureCanReadXML() throws IOException {
        File inputXmlFile = new File(this.getClass().getResource("/cqModel.xml").getFile());
        String xml = new String(Files.readAllBytes(inputXmlFile.toPath()));


        CQLModel model = CQLUtilityClass.getCQLModelFromXML(xml);

        assertEquals("junit", model.getLibraryName());
        assertEquals(4, model.getValueSetList().size());
    }
}