package gov.cms.mat.qdmqicore.conversion.controller;

import gov.cms.mat.fhir.rest.dto.spreadsheet.MatAttribute;
import gov.cms.mat.qdmqicore.mapping.controller.GoogleSpreadsheetController;
import gov.cms.mat.qdmqicore.mapping.service.GoogleSpreadsheetService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversionDataControllerTest {
    @Mock
    private GoogleSpreadsheetService spreadSheetService;
    @InjectMocks
    private GoogleSpreadsheetController conversionDataController;

    @Test
    void getMatAttributes() {
        List<MatAttribute> listToReturn = Collections.singletonList(new MatAttribute());
        when(spreadSheetService.getMatAttributes()).thenReturn(listToReturn);

        assertEquals(listToReturn, conversionDataController.matAttributes());

        verify(spreadSheetService).getMatAttributes();
    }
}