package gov.cms.mat.fhir.services.components.reporting;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class LibraryDataService {

    private static final ThreadLocal<List<LibraryData>> threadLocal = new ThreadLocal<>();

    public void findOrCreate(
            String measureId,
            String matLibraryId,
            LibraryType libraryType,
            String data) {
        var optional = findByIndex(measureId, matLibraryId, libraryType);

        LibraryData libraryData =
                optional.orElseGet(() -> createLibraryData(measureId, matLibraryId, libraryType));

        libraryData.setData(data);
    }

    private LibraryData createLibraryData(String measureId,
                                          String matLibraryId,
                                          LibraryType libraryType) {
        LibraryData libraryData;
        libraryData = new LibraryData();
        libraryData.setMeasureId(measureId);
        libraryData.setMatLibraryId(matLibraryId);
        libraryData.setLibraryType(libraryType);

        List<LibraryData> libraryDataList = threadLocal.get();
        libraryDataList.add(libraryData);

        return libraryData;
    }

    public Optional<LibraryData> findByIndex(String measureId,
                                             String matLibraryId,
                                             LibraryType libraryType) {
        List<LibraryData> libraryDataList = threadLocal.get();

        if (libraryDataList == null) {
            threadLocal.set(new ArrayList<>());
            return Optional.empty();
        } else {
            return libraryDataList.stream()
                    .filter(d -> isSame(d, measureId, matLibraryId, libraryType))
                    .findFirst();
        }
    }

    private boolean isSame(LibraryData data,
                           String measureId, String matLibraryId, LibraryType libraryType) {

        return data.getMeasureId().equals(measureId) &&
                data.getMatLibraryId().equals(matLibraryId) &&
                data.getLibraryType().equals(libraryType);
    }

}
