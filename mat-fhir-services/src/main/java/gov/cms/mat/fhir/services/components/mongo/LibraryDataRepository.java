package gov.cms.mat.fhir.services.components.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface LibraryDataRepository extends MongoRepository<LibraryData, String> {

    Optional<LibraryData> findByConversionResultIdAndMeasureIdAndMatLibraryIdAndLibraryType(String conversionResultId,
                                                                                            String measureId,
                                                                                            String matLibraryId,
                                                                                            LibraryType libraryType);


}
