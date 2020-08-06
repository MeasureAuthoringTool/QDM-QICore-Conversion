package gov.cms.mat.fhir.services.components.reporting;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotBlank;


@Data
@Slf4j
public class LibraryData {
    @NotBlank
    private String measureId;

    @NotBlank
    private LibraryType libraryType;

    @NotBlank
    private String matLibraryId;

    private String data;
}
