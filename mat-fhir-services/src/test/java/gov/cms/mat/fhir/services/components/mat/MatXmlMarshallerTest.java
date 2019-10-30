package gov.cms.mat.fhir.services.components.mat;

import mat.client.measure.ManageCompositeMeasureDetailModel;
import mat.model.cql.CQLQualityDataModelWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MatXmlMarshallerTest {
    private MatXmlMarshaller matXmlMarshaller;

    @BeforeEach
    void setUp() {
        matXmlMarshaller = new MatXmlMarshaller();
    }

    @Test
    void toCompositeMeasureDetail() {
        String xml = getXml("/measureDetail.xml");
        ManageCompositeMeasureDetailModel model = matXmlMarshaller.toCompositeMeasureDetail(xml);
        assertNotNull(model.getId());
    }

    @Test
    void toQualityData() {
        String xml = getXml("/cqlLookUp.xml");
        CQLQualityDataModelWrapper model = matXmlMarshaller.toQualityData(xml);
        assertFalse(model.getQualityDataDTO().isEmpty());
    }

    private String getXml(String resource) {
        File inputXmlFile = new File(this.getClass().getResource(resource).getFile());

        try {
            return new String(Files.readAllBytes(inputXmlFile.toPath()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}