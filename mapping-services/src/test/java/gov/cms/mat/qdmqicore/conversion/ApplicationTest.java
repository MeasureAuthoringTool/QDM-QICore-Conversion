package gov.cms.mat.qdmqicore.conversion;

import gov.cms.mat.qdmqicore.mapping.service.GoogleSpreadsheetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ApplicationTest {
    @Autowired
    private GoogleSpreadsheetService spreadsheetService;

//    @Test
//    void contextLoads() {
//        assertTrue(spreadsheetService.getMatAttributes() != null);
//    }
}