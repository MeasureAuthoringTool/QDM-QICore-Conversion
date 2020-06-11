package gov.cms.mat.fhir.services.rest.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import mat.shared.CQLError;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode( exclude = "errors")
public class LibraryErrors {
    @NotBlank
    private String name;
    @NotBlank
    private String version;

    public LibraryErrors(@NotBlank String name, @NotBlank String version) {
        this.name = name;
        this.version = version;
    }

    List<CQLError> errors = new ArrayList<>();
}
