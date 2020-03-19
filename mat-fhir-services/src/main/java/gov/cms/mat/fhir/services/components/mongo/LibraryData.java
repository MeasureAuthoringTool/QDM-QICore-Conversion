package gov.cms.mat.fhir.services.components.mongo;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import java.time.Instant;

@Document
@CompoundIndex(name = "library_data-uniq-idx", unique = true,
        def = "{'conversionResultId' : 1, 'measureId' : 1, 'matLibraryId' : 1, 'libraryType' : 1}")
@Data
@Slf4j
public class LibraryData {
    @Id
    private String id;
    @CreatedDate
    private Instant created;

    @NotBlank
    private String conversionResultId;

    @NotBlank
    private String measureId;

    @NotBlank
    private LibraryType libraryType;

    @NotBlank
    private String matLibraryId;

    private String data;
}
