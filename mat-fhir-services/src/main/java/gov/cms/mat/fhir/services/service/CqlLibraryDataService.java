package gov.cms.mat.fhir.services.service;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.services.exceptions.CqlLibraryNotFoundException;
import gov.cms.mat.fhir.services.repository.CqlLibraryRepository;
import gov.cms.mat.fhir.services.summary.CqlLibraryFindData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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

    public CqlLibrary findCqlLibrary(CqlLibraryFindData cqlLibraryFindData) {  //todo MCG return array can be multiples

        List<CqlLibrary> libraries =
                cqlLibraryRepo.findByQdmVersionAndCqlNameAndVersionAndFinalizedDateIsNotNull(
                        cqlLibraryFindData.getQdmVersion(),
                        cqlLibraryFindData.getName(),
                        cqlLibraryFindData.getVersion());

        if (libraries.isEmpty()) {
            throw new CqlLibraryNotFoundException(cqlLibraryFindData);
        } else if (libraries.size() > 1) {

            String ids = libraries.stream()
                    .map(CqlLibrary::getId)
                    .collect(Collectors.joining(", "));

            throw new CqlLibraryNotFoundException("To many cql libraries found ids: ", ids);
        } else {
            return libraries.get(0);
        }
    }
}
