package gov.cms.mat.fhir.services.service;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.commons.model.CqlLibraryAssociation;
import gov.cms.mat.fhir.services.exceptions.CqlLibraryNotFoundException;
import gov.cms.mat.fhir.services.repository.CqlLibraryAssociationRepository;
import gov.cms.mat.fhir.services.repository.CqlLibraryRepository;
import gov.cms.mat.fhir.services.summary.CqlLibraryFindData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CqlLibraryDataService {
    private final CqlLibraryRepository cqlLibraryRepo;

    private final CqlLibraryAssociationRepository cqlLibraryAssociationRepository;

    public CqlLibraryDataService(CqlLibraryRepository cqlLibraryRepo, CqlLibraryAssociationRepository cqlLibraryAssociationRepository) {
        this.cqlLibraryRepo = cqlLibraryRepo;
        this.cqlLibraryAssociationRepository = cqlLibraryAssociationRepository;
    }

    public List<CqlLibrary> getCqlLibrariesByMeasureIdRequired(String measureId) {
        List<CqlLibrary> cqlLibs = cqlLibraryRepo.getCqlLibraryByMeasureId(measureId);

        if (cqlLibs.isEmpty()) {
            return findLibrariesByAssociation(measureId);
        } else {
            return cqlLibs;
        }
    }

    private List<CqlLibrary> findLibrariesByAssociation(String measureId) {
        List<CqlLibraryAssociation> associations = cqlLibraryAssociationRepository.findByAssociationId(measureId);

        List<CqlLibrary> cqlLibraries = associations.stream()
                .map(CqlLibraryAssociation::getCqlLibraryId)
                .map(cqlLibraryRepo::getCqlLibraryById)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (cqlLibraries.isEmpty()) {
            throw new CqlLibraryNotFoundException(measureId);
        } else {
            return cqlLibraries;
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
                .orElseThrow(() -> new CqlLibraryNotFoundException("Too many cql libraries found"));
    }
}
