package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.services.repository.CqlLibraryExportRepository;
import gov.cms.mat.fhir.services.repository.CqlLibraryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/cql/file/generator")
@RequiredArgsConstructor
public class CqlFileGenerator {

    private static final String DASH_LINE = "---------------------------------";

    @Value("${measures.allowed.versions}")
    private List<String> allowedMeasuresVersions;

    @Value("${output.directory}")
    private String tmpOutputDirectory;

    private final CqlLibraryExportRepository cqlLibraryExportRepo;

    private final CqlLibraryRepository cqlLibraryRepo;

    @GetMapping
    public void generateCqlFiles() {
        Path outputDir = createOutputDirectory();

        List<CqlLibrary> allowedCqlLibraryIds = findAllAllowedCqlLibraryIds();

//        List<String> allCqls = new ArrayList<>();
//        List<String> IdsWithoutCqlExports = new ArrayList<>();
        allowedCqlLibraryIds.stream()
                .limit(20)
                .forEach(cqlLibrary -> processLibraryId(outputDir, cqlLibrary));

//        log.info(DASH_LINE);
//        log.warn("There were [{}] CQL_LIBRARY_IDs without a row in CQL_LIBRARY_EXPORT table. They are as follows: ", IdsWithoutCqlExports.size());
//        log.warn("{}", IdsWithoutCqlExports);
//
//        log.info(DASH_LINE);
//
//        log.info(DASH_LINE);
//        log.info("Found {} CQLs in total from all the allowed CqlLibraries", allCqls.size());
//        log.info(DASH_LINE);

        log.info(DASH_LINE);
        log.info("Done.");
        log.info(DASH_LINE);
    }

    private Path createOutputDirectory() {
        log.debug(DASH_LINE);
        log.debug("OutputDirectory : {}", tmpOutputDirectory);

        Path outputDir;
        Path outputDirPath = Paths.get(tmpOutputDirectory, "" + Instant.now().getNano());
        try {
            outputDir = Files.createDirectories(outputDirPath);
            log.info("Created directory : [{}] ", outputDir.toAbsolutePath().toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return outputDir;
    }

    private List<CqlLibrary> findAllAllowedCqlLibraryIds() {
        List<CqlLibrary> allowedCqlLibraryIds = cqlLibraryRepo.findAllowedCqlLibraries(allowedMeasuresVersions);

        log.info(DASH_LINE);
        log.info("Found [{}] CqlLibraries with allowedMeasureVersions", allowedCqlLibraryIds.size(), allowedMeasuresVersions);
        log.info(DASH_LINE);

        return allowedCqlLibraryIds;
    }



    private void processLibraryId(Path outputDir, CqlLibrary cqlLibrary) {
        List<byte[]> exportCQLs = cqlLibraryExportRepo.findByCqlLibraryId(cqlLibrary.getId());
        if (exportCQLs.size() > 1) {
            log.info(DASH_LINE);
            log.info("Found more {} Cql_library_exports for cql_Library_Id", exportCQLs.size(), cqlLibrary.getId());
            log.info(DASH_LINE);
            throw new RuntimeException("Found Multiple cql_library_exports rows");
        }
        if (!exportCQLs.isEmpty()) {
            writeToFile(outputDir, cqlLibrary, exportCQLs);
        }
    }
    private void writeToFile(Path outputDirPath, CqlLibrary cqlLibrary, List<byte[]> exportCQLs) {
        if (exportCQLs.size() != 1) {
            var errMsg = "Multiple CQL_LIBRARY_EXPORT rows found for CQL_LIBRARY_ID: [{}]" + cqlLibrary.getId();
            throw new RuntimeException(errMsg);
        }

        String filename = toFilename(cqlLibrary);
        Path outputFilePath = Paths.get(outputDirPath.toString(), filename);

        try {
            Path file = Files.createFile(outputFilePath);
            Files.write(file, exportCQLs.get(0));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        log.info("created : [{}]", outputFilePath.toFile().getName());
    }

    private String toFilename(CqlLibrary cqlLibrary) {
        return new StringBuilder()
                .append(cqlLibrary.getCqlName())
                .append(cqlLibrary.getDraft() ? "_Draft_" :"_")
                .append(cqlLibrary.getVersion())
                .append(".cql")
                .toString();
    }
}
