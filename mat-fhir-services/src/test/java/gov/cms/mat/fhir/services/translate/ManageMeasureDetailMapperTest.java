package gov.cms.mat.fhir.services.translate;

import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.services.components.mat.MatXmlConverter;
import gov.cms.mat.fhir.services.components.mat.MatXmlException;
import mat.client.measure.ManageCompositeMeasureDetailModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManageMeasureDetailMapperTest {
    private static final String XML_DATA = "<strong>coffee</strong>";

    @Mock
    private MatXmlConverter matXmlConverter;
    @InjectMocks
    private ManageMeasureDetailMapper manageMeasureDetailMapper;


    @BeforeEach
    void setUp() throws IOException {
        //  File inputXmlFile = new File(this.getClass().getResource("/measureExportSimple.xml").getFile());
        // xmlBytes = Files.readAllBytes(inputXmlFile.toPath());
    }

    @Test
    void testConvert_SendEmptyBytes() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            manageMeasureDetailMapper.convert(new byte[0], new Measure());
        });

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            manageMeasureDetailMapper.convert(null, new Measure());
        });

        verifyNoInteractions(matXmlConverter);
    }

    @Test
    void testConvert_MatXmlConverter_ReturnsNull() {
        when(matXmlConverter.toCompositeMeasureDetail(anyString())).thenReturn(null);

        Assertions.assertThrows(MatXmlException.class, () -> {
            manageMeasureDetailMapper.convert(XML_DATA.getBytes(), new Measure());
        });

        verify(matXmlConverter).toCompositeMeasureDetail(XML_DATA);
    }

    @Test /* currently tests with empty objects to avoid Null Pointer Exceptions */
    void testConvert_MatXmlConverter_ReturnsManageCompositeMeasureDetailModel() {
        ManageCompositeMeasureDetailModel modelToReturn = new ManageCompositeMeasureDetailModel();
        when(matXmlConverter.toCompositeMeasureDetail(anyString())).thenReturn(modelToReturn);

        ManageCompositeMeasureDetailModel modelReturned =
                manageMeasureDetailMapper.convert(XML_DATA.getBytes(), new Measure());

        assertEquals(modelToReturn, modelReturned);

        verify(matXmlConverter).toCompositeMeasureDetail(XML_DATA);
    }
}