package gov.cms.mat.fhir.services.components.mongo;

import gov.cms.mat.fhir.rest.dto.LibraryConversionResults;
import gov.cms.mat.fhir.rest.dto.MeasureConversionResults;
import gov.cms.mat.fhir.rest.dto.ValueSetConversionResults;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import java.time.Instant;

@Document
@Data
public class ConversionResult {
    ValueSetConversionResults valueSetConversionResults;
    MeasureConversionResults measureConversionResults;
    LibraryConversionResults libraryConversionResults;
    @Id
    private String id;
    @Version
    private Long version;
    @CreatedDate
    private Instant created;
    @LastModifiedDate
    private Instant modified;
    @NotBlank
    @Indexed(unique = true)
    private String measureId;
}
