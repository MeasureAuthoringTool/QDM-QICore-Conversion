package gov.cms.mat.fhir.services.components.library;

import gov.cms.mat.fhir.services.components.mongo.UnconvertedCqlLibraryRepository;
import gov.cms.mat.fhir.services.components.mongo.UnconvertedCqlLibraryResult;
import gov.cms.mat.fhir.services.exceptions.LibraryFileNotFoundException;
import gov.cms.mat.fhir.services.summary.CqlLibraryFindData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class UnConvertedCqlLibraryHandler implements FileHandler {
    private static final String EXTENSION = ".cql";
    private final UnconvertedCqlLibraryRepository unconvertedCqlLibraryRepository;

    public UnConvertedCqlLibraryHandler(UnconvertedCqlLibraryRepository unconvertedCqlLibraryRepository) {
        this.unconvertedCqlLibraryRepository = unconvertedCqlLibraryRepository;
    }

    public void write(CqlLibraryFindData findData, String cql) {
        try {
            UnconvertedCqlLibraryResult unconvertedCqlLibraryResult = new UnconvertedCqlLibraryResult();
            unconvertedCqlLibraryResult.setName(makeCqlName(findData));
            unconvertedCqlLibraryResult.setFindData(findData);
            unconvertedCqlLibraryResult.setCql(cql);

            unconvertedCqlLibraryRepository.save(unconvertedCqlLibraryResult);
        } catch (Exception e) {
            log.error("Cannot save {} UnconvertedCqlLibraryResult : {} ", makeCqlName(findData), findData, e);
        }
    }

    public boolean exists(CqlLibraryFindData findData) {
        return unconvertedCqlLibraryRepository.findByName(makeCqlName(findData)).isPresent();
    }

    public String makeCqlName(CqlLibraryFindData data) {
        return data.getName() + "_" + data.getQdmVersion() + "_" + data.getVersion() + EXTENSION;
    }

    public String findCql(String name) {
        Optional<UnconvertedCqlLibraryResult> optional = unconvertedCqlLibraryRepository.findByName(name);

        if (optional.isPresent()) {
            return optional.get().getCql();
        } else {
            throw new LibraryFileNotFoundException(name);
        }
    }

    public List<String> findAll() {
        return unconvertedCqlLibraryRepository.findAll().stream()
                .map(UnconvertedCqlLibraryResult::getName)
                .collect(Collectors.toList());
    }
}
