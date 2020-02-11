package gov.cms.mat.fhir.services.components.library;

import gov.cms.mat.fhir.services.config.LibraryConversionFileConfig;
import gov.cms.mat.fhir.services.summary.CqlLibraryFindData;
import lombok.extern.slf4j.Slf4j;
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
    private final LibraryConversionFileConfig libraryConversionFileConfig;
    private Path path;

    public UnConvertedCqlLibraryFileHandler(LibraryConversionFileConfig libraryConversionFileConfig) {
        this.libraryConversionFileConfig = libraryConversionFileConfig;
    }


    @PostConstruct
    public void check() {
        path = checkAndCreatePath(libraryConversionFileConfig.getUnconvertedDirectory());
        log.info("Cql unconverted directory is: {}", libraryConversionFileConfig.getUnconvertedDirectory());
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
