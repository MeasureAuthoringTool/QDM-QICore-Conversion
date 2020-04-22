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

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/cql/file/generator")
public class CqlFileGenerator {

    @Value("${measures.allowed.versions}")
    private List<String> allowedMeasuresVersions;
//    private List<String> allowedMeasuresVersion = List.of("v5.5", "v5.6", "v5.7", "v5.8", "v5.9", "v6.0");

    @Autowired
    private CqlLibraryExportRepository cqlLibraryExportRepo;

    @Autowired
    private CqlLibraryRepository cqlLibraryRepo;

    @GetMapping
    public void generateCqlFiles() {
        log.info("---------------------------------");
        log.info("inside generateCqlFiles() . ...");
        log.info("---------------------------------");

        log.info("allowedMeasuresVersion : {}", allowedMeasuresVersions);
        log.info("---------------------------------");

        List<CqlLibrary> allowedCqlLibraryIds = cqlLibraryRepo.findAllowedCqlLibraries(allowedMeasuresVersions);
        log.info("---------------------------------");
        log.info("allowedCqlLibraries count : {}", allowedCqlLibraryIds.size());

        List<String> allCqls = new ArrayList<>();
        allowedCqlLibraryIds.stream().forEach(cqlLibrary -> {
            List<String> exportCQLs = cqlLibraryExportRepo.findByCqlLibraryId(cqlLibrary.getId());
            if (exportCQLs.size() > 1) {
                log.info("Found more {} Cql_library_exports for cql_Library_Id", exportCQLs.size(), cqlLibrary.getId());
                throw new RuntimeException("Found Multiple cql_library_exports rows");
            }
            if (!exportCQLs.isEmpty()) {
                allCqls.addAll(exportCQLs);
            }

        });
        log.info("---------------------------------");
        log.info("Found {} CQLs in total from all the allowed CqlLibraries", allCqls.size());


//        List<CqlLibraryExport> exports = cqlLibraryExportRepo.findByCqlLibraryId()
//        log.info("---------------------------------");
//        log.info(" Found : {} exports records ", exports.size());
//        log.info("---------------------------------");
    }
}
