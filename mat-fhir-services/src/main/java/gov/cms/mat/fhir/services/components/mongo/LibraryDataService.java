package gov.cms.mat.fhir.services.components.mongo;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LibraryDataService {

    private final LibraryDataRepository libraryDataRepository;

    public LibraryDataService(LibraryDataRepository libraryDataRepository) {
        this.libraryDataRepository = libraryDataRepository;
    }

    LibraryData findOrCreate(String conversionResultId,
                             String measureId,
                             String matLibraryId,
                             LibraryType libraryType,
                             String data) {
        var optional = findByIndex(conversionResultId, measureId, matLibraryId, libraryType);

        LibraryData libraryData = optional
                .orElseGet(() -> createLibraryData(conversionResultId, measureId, matLibraryId, libraryType));

        libraryData.setData(data);
        return libraryDataRepository.save(libraryData);
    }

    private LibraryData createLibraryData(String conversionResultId,
                                          String measureId,
                                          String matLibraryId,
                                          LibraryType libraryType) {
        LibraryData libraryData;
        libraryData = new LibraryData();
        libraryData.setConversionResultId(conversionResultId);
        libraryData.setMeasureId(measureId);
        libraryData.setMatLibraryId(matLibraryId);
        libraryData.setLibraryType(libraryType);
        return libraryData;
    }


    Optional<LibraryData> findByIndex(String conversionResultId,
                                      String measureId,
                                      String matLibraryId,
                                      LibraryType libraryType) {
        return libraryDataRepository.findByConversionResultIdAndMeasureIdAndMatLibraryIdAndLibraryType(
                conversionResultId,
                measureId,
                matLibraryId,
                libraryType);
    }

}
