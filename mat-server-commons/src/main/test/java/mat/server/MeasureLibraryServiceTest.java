package mat.server;

import mat.client.measure.ManageCompositeMeasureDetailModel;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.Assert.assertNotNull;

public class MeasureLibraryServiceTest {

    @Test
    public void createModelFromXML() throws IOException {
        File inputXmlFile = new File(this.getClass().getResource("/manageMeasureDetail.xml").getFile());
        String xml = new String(Files.readAllBytes(inputXmlFile.toPath()));

        ManageCompositeMeasureDetailModel manageMeasureDetailModel = MeasureLibraryService.createModelFromXML(xml);
        assertNotNull(manageMeasureDetailModel);

    }
}