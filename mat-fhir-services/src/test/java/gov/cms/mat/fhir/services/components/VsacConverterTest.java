package gov.cms.mat.fhir.services.components;

import mat.model.VSACValueSetWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VsacConverterTest {
    private String xml;
    private VsacConverter vsacConverter;

    @BeforeEach
    void setUp() throws IOException {
        File inputXmlFile = new File(this.getClass().getResource("/vsacResponseResult.xml").getFile());
        xml = new String(Files.readAllBytes(inputXmlFile.toPath()));

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
    void toWrapper_Success() {
        VSACValueSetWrapper vsacValueSetWrapper = vsacConverter.toWrapper(xml);
        assertNotNull(vsacValueSetWrapper);
    }
}