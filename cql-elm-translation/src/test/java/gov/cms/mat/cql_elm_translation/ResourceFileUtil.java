package gov.cms.mat.cql_elm_translation;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;

public interface ResourceFileUtil {
    default String getData(String resource) {
        File inputXmlFile = new File(this.getClass().getResource(resource).getFile());

        try {
            return new String(Files.readAllBytes(inputXmlFile.toPath()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
