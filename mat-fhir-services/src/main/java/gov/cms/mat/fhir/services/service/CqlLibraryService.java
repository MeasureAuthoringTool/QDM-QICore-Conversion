package gov.cms.mat.fhir.services.service;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.services.exceptions.CqlLibraryNotFoundException;
import gov.cms.mat.fhir.services.repository.CqlLibraryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
public class CqlLibraryService {
    private final CqlLibraryRepository cqlLibraryRepo;

    public CqlLibraryService(CqlLibraryRepository cqlLibraryRepo) {
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

    public CqlLibrary getCqlLibraryById(String id) {
        return cqlLibraryRepo.getCqlLibraryById(id);
    }

    public CqlLibrary getCqlLibraryByNameAndVersion(String cqlName, BigDecimal version) {
        return cqlLibraryRepo.getCqlLibraryByNameAndVersion(cqlName, version);
    }
}
