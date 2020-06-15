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
    private final CqlLibraryRepository cqlLibraryRepository;

    public CqlLibraryDataService(CqlLibraryRepository cqlLibraryRepository) {
        this.cqlLibraryRepository = cqlLibraryRepository;
    }

    public CqlLibrary getMeasureLib(String measureId) {
        return cqlLibraryRepository.getCqlLibraryByMeasureId(measureId);
    }


    public CqlLibrary findCqlLibrary(CqlLibraryFindData cqlLibraryFindData) {
        List<CqlLibrary> libraries;

        if (cqlLibraryFindData.getType().equals("QDM")) {
            libraries =
                    cqlLibraryRepository.findByQdmVersionAndCqlNameAndVersionAndLibraryModelAndFinalizedDateIsNotNull(
                            cqlLibraryFindData.getQdmVersion(),
                            cqlLibraryFindData.getName(),
                            cqlLibraryFindData.getMatVersion(),
                            cqlLibraryFindData.getType());

        } else if (cqlLibraryFindData.getType().equals("FHIR")) {
            libraries =
                    cqlLibraryRepository.findByVersionAndCqlNameAndRevisionNumberAndLibraryModel(
                            cqlLibraryFindData.getPair().getLeft(),
                            cqlLibraryFindData.getName(),
                            cqlLibraryFindData.getPair().getRight(),
                            cqlLibraryFindData.getType());
        } else {
            throw new IllegalArgumentException("Invalid library type: " + cqlLibraryFindData.getType());
        }

        return processLibraries(cqlLibraryFindData, libraries);
    }

    private CqlLibrary processLibraries(CqlLibraryFindData cqlLibraryFindData, List<CqlLibrary> libraries) {
        if (libraries.isEmpty()) {
            throw new CqlLibraryNotFoundException(cqlLibraryFindData);
        } else if (libraries.size() > 1) {
            return findLatestByFinalizedDate(libraries);
        } else {
            return libraries.get(0);
        }
    }

    private CqlLibrary findLatestByFinalizedDate(List<CqlLibrary> libraries) {
        return libraries.stream()
                .max(Comparator.comparing(CqlLibrary::getFinalizedDate))
                .orElseThrow(() -> new CqlLibraryNotFoundException("Too many cql libraries found"));
    }

    public CqlLibrary findCqlLibraryRequired(String id) {
        var optional = cqlLibraryRepository.findById(id);

        if (optional.isEmpty()) {
            throw new CqlLibraryNotFoundException("Cannot find cqlLibrary with ", id);
        }

        return optional.get();
    }
}
