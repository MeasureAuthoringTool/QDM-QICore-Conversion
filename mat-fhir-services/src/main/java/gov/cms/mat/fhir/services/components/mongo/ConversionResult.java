package gov.cms.mat.fhir.services.components.mongo;

import gov.cms.mat.fhir.rest.cql.CqlConversionResult;
import gov.cms.mat.fhir.rest.cql.LibraryConversionResults;
import gov.cms.mat.fhir.rest.cql.MeasureConversionResults;
import gov.cms.mat.fhir.rest.cql.ValueSetConversionResults;
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
    @Id
    private String id;
    @Version
    private Long version;
    @CreatedDate
    private Instant created;
    @LastModifiedDate
    private Instant modified;

    ValueSetConversionResults valueSetConversionResults;
    MeasureConversionResults measureConversionResults;
    LibraryConversionResults libraryConversionResults;
    @NotBlank
    @Indexed(unique = true)
    private String measureId;


//    private List<ValueSetResult> valueSetResults = new ArrayList<>();
//    private ConversionType valueSetConversionType;
//    private List<ValueSetValidationResult> valueSetFhirValidationErrors = new ArrayList<>();

    // private List<FieldConversionResult> measureResults = new ArrayList<>();
    // private ConversionType measureConversionType;
    //  private List<FhirValidationResult> measureFhirValidationErrors = new ArrayList<>();

//    private List<FieldConversionResult> libraryResults = new ArrayList<>();
//    private ConversionType libraryConversionType;
//    private List<FhirValidationResult> libraryFhirValidationErrors = new ArrayList<>();


    private CqlConversionResult cqlConversionResult; // add me to the library uber object
}
