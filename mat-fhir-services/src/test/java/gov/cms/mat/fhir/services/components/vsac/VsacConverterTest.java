package gov.cms.mat.fhir.services.components.vsac;

import gov.cms.mat.fhir.services.ResourceFileUtil;
import mat.model.VSACValueSetWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VsacConverterTest implements ResourceFileUtil {
    private String xml;
    private VsacConverter vsacConverter;

    @BeforeEach
    void setUp() throws IOException {
        xml = getStringFromResource("/vsacResponseResult.xml");

        vsacConverter = new VsacConverter();
    }

    @Test
    void toWrapper_CheckEmpty() {
        assertThrows(UncheckedIOException.class, () -> {
            vsacConverter.toWrapper(null);
        });

        assertThrows(UncheckedIOException.class, () -> {
            vsacConverter.toWrapper("");
        });
    }

    @Test
    void toWrapper_MarshalError() {
        assertThrows(UncheckedIOException.class, () -> {
            vsacConverter.toWrapper("{Json:'isBetter'}");
        });
    }

    @Test
    void toWrapper_Success() {
        VSACValueSetWrapper vsacValueSetWrapper = vsacConverter.toWrapper(xml);
        assertNotNull(vsacValueSetWrapper);
    }
}