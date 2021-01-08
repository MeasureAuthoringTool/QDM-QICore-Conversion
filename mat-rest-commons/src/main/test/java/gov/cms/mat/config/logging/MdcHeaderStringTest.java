package gov.cms.mat.config.logging;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MdcHeaderStringTest {

    @Test
    void createEmpty() {
      var optional =   MdcHeaderString.create();
      assertTrue(optional.isEmpty());
    }


}