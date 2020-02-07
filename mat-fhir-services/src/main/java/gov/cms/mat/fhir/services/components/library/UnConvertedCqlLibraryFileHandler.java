package gov.cms.mat.fhir.services.components.library;

import gov.cms.mat.fhir.services.summary.CqlLibraryFindData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Component
@Slf4j
public class UnConvertedCqlLibraryFileHandler implements FileHandler {
    private static final String EXTENSION = ".cql";
    private Path path;

    @Value("${library.unconverted.directory}")
    private String directoryName;

    @PostConstruct
    public void check() {
        path = checkAndCreatePath(directoryName);
        log.info("Cql unconverted directory is: {}", directoryName);
    }

    public boolean exists(CqlLibraryFindData findData) {
        return makeFilePath(findData)
                .toFile()
                .exists();
    }

    public void write(CqlLibraryFindData findData, String cql) {
        try {
            Files.write(makeFilePath(findData), cql.getBytes());
        } catch (IOException e) {
            log.error("Cannot write file: {} ", makeCqlFileName(findData));
        }
    }

    private Path makeFilePath(CqlLibraryFindData data) {
        return path.resolve(makeCqlFileName(data));
    }

    public String makeCqlFileName(CqlLibraryFindData data) {
        return data.getName() + "_" + data.getQdmVersion() + "_" + data.getVersion() + EXTENSION;
    }

    public String findCql(String name) {
        return findCqlInFile(path, name);
    }

    public List<String> findAll() {
        return findAll(path);
    }
}
