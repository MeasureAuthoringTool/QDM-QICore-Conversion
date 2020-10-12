package gov.cms.mat.patients.conversion;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;

public interface ResourceFileUtil {
    default String getStringFromResource(String resource) {
        File inputXmlFile = new File(this.getClass().getResource(resource).getFile());

        try {
            return new String(Files.readAllBytes(inputXmlFile.toPath()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
