package gov.cms.mat.fhir.services.translate;

import gov.cms.mat.fhir.commons.model.Measure;
import mat.client.measure.ManageCompositeMeasureDetailModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ManageMeasureDetailMapperTest {
    private byte[] xmlBytes;

    @BeforeEach
    void setUp() throws IOException {
        File inputXmlFile = new File(this.getClass().getResource("/measureExportSimple.xml").getFile());
        xmlBytes = Files.readAllBytes(inputXmlFile.toPath());
    }

    @Test
    void testConvertWithEmptyMeasure() {

        Measure measure = new Measure();
        ManageMeasureDetailMapper manageMeasureDetailMapper = new ManageMeasureDetailMapper(xmlBytes, measure);

        ManageCompositeMeasureDetailModel model = manageMeasureDetailMapper.convert();

        assertNotNull(model);
    }
}