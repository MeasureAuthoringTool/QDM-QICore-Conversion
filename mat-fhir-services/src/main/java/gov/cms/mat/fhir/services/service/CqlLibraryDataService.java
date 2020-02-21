package gov.cms.mat.fhir.services.service;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.services.exceptions.CqlLibraryNotFoundException;
import gov.cms.mat.fhir.services.repository.CqlLibraryRepository;
import gov.cms.mat.fhir.services.summary.CqlLibraryFindData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
public class CqlLibraryDataService {
    private final CqlLibraryRepository cqlLibraryRepo;

    public CqlLibraryDataService(CqlLibraryRepository cqlLibraryRepo) {
        this.cqlLibraryRepo = cqlLibraryRepo;
    }

    public List<CqlLibrary> getCqlLibrariesByMeasureIdRequired(String id) {
        List<CqlLibrary> cqlLibs = cqlLibraryRepo.getCqlLibraryByMeasureId(id);

        if (cqlLibs.isEmpty()) {
            throw new CqlLibraryNotFoundException(id);
        } else {
            return cqlLibs;
        }
    }

    public CqlLibrary findCqlLibrary(CqlLibraryFindData cqlLibraryFindData) {

        List<CqlLibrary> libraries =
                cqlLibraryRepo.findByQdmVersionAndCqlNameAndVersionAndFinalizedDateIsNotNull(
                        cqlLibraryFindData.getQdmVersion(),
                        cqlLibraryFindData.getName(),
                        cqlLibraryFindData.getMatVersion());

        if (libraries.isEmpty()) {
            throw new CqlLibraryNotFoundException(cqlLibraryFindData);
        } else if (libraries.size() > 1) {
            return findLatestByFinalizedDate(libraries);
        } else {
            return libraries.get(0);
        }
    }

    public CqlLibrary findLatestByFinalizedDate(List<CqlLibrary> libraries) {
        return libraries.stream()
                .max(Comparator.comparing(CqlLibrary::getFinalizedDate))
                .orElseThrow(() -> new CqlLibraryNotFoundException( "Too many cql libraries found"));
    }
}
