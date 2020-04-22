package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.commons.model.CqlLibraryExport;
import gov.cms.mat.fhir.services.repository.CqlLibraryExportRepository;
import gov.cms.mat.fhir.services.repository.CqlLibraryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
public class CqlFileGenerator {

    private static final String DASH_LINE = "---------------------------------";
    @Value("${measures.allowed.versions}")
    private List<String> allowedMeasuresVersions;
//    private List<String> allowedMeasuresVersion = List.of("v5.5", "v5.6", "v5.7", "v5.8", "v5.9", "v6.0");

    @Value("${efs.mount}")
    private String efsMount;

    @Autowired
    private CqlLibraryExportRepository cqlLibraryExportRepo;

    @Autowired
    private CqlLibraryRepository cqlLibraryRepo;

    @GetMapping
    public void generateCqlFiles() {
        logAllowedVersions();

        Path outputDir = createOutputDirectory();

        List<CqlLibrary> allowedCqlLibraryIds = findAllAllowedCqlLibraryIds();

        List<String> allCqls = new ArrayList<>();
        List<String> IdsWithoutCqlExports = new ArrayList<>();
        allowedCqlLibraryIds.stream()
                .limit(20)
                .forEach(cqlLibrary -> processLibraryId(outputDir, allCqls, IdsWithoutCqlExports, cqlLibrary));

        log.info(DASH_LINE);
        log.warn("There were [{}] CQL_LIBRARY_IDs without a row in CQL_LIBRARY_EXPORT table. They are as follows: ", IdsWithoutCqlExports.size());
        log.warn("{}", IdsWithoutCqlExports);

        log.info(DASH_LINE);

        log.info(DASH_LINE);
        log.info("Found {} CQLs in total from all the allowed CqlLibraries", allCqls.size());
        log.info(DASH_LINE);
    }

    private void logAllowedVersions() {
        log.debug(DASH_LINE);
        log.debug("allowedMeasuresVersion : {}", allowedMeasuresVersions);
        log.debug(DASH_LINE);
    }

    private Path createOutputDirectory() {
        log.debug(DASH_LINE);
        log.debug("efsMount : {}", efsMount);
        log.debug(DASH_LINE);

        Path outputDir;
        Path outputDirPath = Paths.get(efsMount, "" + Instant.now().getNano());
        try {
            outputDir = Files.createDirectories(outputDirPath);
            log.info("Created directory : [{}]", outputDir.toAbsolutePath().toString());
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



    private void processLibraryId(Path outputDir, List<String> allCqls, List<String> idsWithoutCqlExports, CqlLibrary cqlLibrary) {
        List<byte[]> exportCQLs = cqlLibraryExportRepo.findByCqlLibraryId(cqlLibrary.getId());
        if (exportCQLs.size() > 1) {
            log.info(DASH_LINE);
            log.info("Found more {} Cql_library_exports for cql_Library_Id", exportCQLs.size(), cqlLibrary.getId());
            log.info(DASH_LINE);
            throw new RuntimeException("Found Multiple cql_library_exports rows");
        }
        if (exportCQLs.isEmpty()) {
            idsWithoutCqlExports.add(cqlLibrary.getId());

        } else {

            String cql = writeToFile(outputDir, cqlLibrary, exportCQLs);
            allCqls.add(cql);
        }
    }
    private String writeToFile(Path outputDirPath, CqlLibrary cqlLibrary, List<byte[]> exportCQLs) {
        if (exportCQLs.size() != 1) {
            var errMsg = "Multiple CQL_LIBRARY_EXPORT rows found for CQL_LIBRARY_ID: [{}]" + cqlLibrary.getId();
            throw new RuntimeException(errMsg);
        }
        String cql = new String(exportCQLs.get(0));

        String filename = toFilename(cqlLibrary);
        Path outputFilePath = Paths.get(outputDirPath.toString(), filename);

        System.out.println("created \t\t ...: " + outputFilePath.toFile().getName());
        System.out.println("created \t\t ...: " + outputFilePath.toAbsolutePath().toString());
//        System.out.println(outputFilePath.toAbsolutePath().toString() + "\t\t ... \t\tcreated");
//        log.info("{} \t\t CQL : [{}]", outputFilePath.toFile().getName(), cql.substring(0, 100));
//        System.out.println(outputFilePath.toFile().getName() + "\t\t CQL : " + cql.substring(0, 100));
//        log.info("{} \t\t\t created with CQL : [{}]", outputFilePath.toAbsolutePath(), cql.substring(0, 50));
        try {
            Path file = Files.createFile(outputFilePath);
            Files.write(file, exportCQLs.get(0));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return cql;
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
