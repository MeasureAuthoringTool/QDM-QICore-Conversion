package gov.cms.mat.fhir.services.components.mongo;

import gov.cms.mat.fhir.rest.cql.MatCqlConversionException;
import gov.cms.mat.fhir.services.service.support.CqlConversionError;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    private List<ValueSetResult> valueSetResults = new ArrayList<>();
    private ConversionType valueSetConversionType;
    private List<ValueSetValidationResult> valueSetFhirValidationErrors = new ArrayList<>();

    private List<FieldConversionResult> measureResults = new ArrayList<>();
    private ConversionType measureConversionType;
    private List<FhirValidationResult> measureFhirValidationErrors = new ArrayList<>();

    private List<FieldConversionResult> libraryResults = new ArrayList<>();
    private ConversionType libraryConversionType;
    private List<FhirValidationResult> libraryFhirValidationErrors = new ArrayList<>();

    private CqlConversionResult cqlConversionResult; // add me to the library uber object


    @NotBlank
    @Indexed(unique = true)
    private String measureId;

    @Data
    @Builder
    public static class FieldConversionResult {
        String field;
        String destination;
        String reason;
    }

    @Data
    @Builder
    public static class ValueSetResult {
        String oid;
        String reason;
    }

    @Data
    public static class CqlConversionResult {
        ConversionType type;
        Boolean result;
        List<String> errors;
        String cql;
        String elm;
        List<CqlConversionError> cqlConversionErrors;
        Set<MatCqlConversionException> matCqlConversionErrors = new HashSet<>();
    }

    @Data
    @Builder
    public static class FhirValidationResult {
        String severity;
        String locationField;
        String errorDescription;
    }

    @Data
    @RequiredArgsConstructor
    public static class ValueSetValidationResult {
        @NonNull
        final String oid;
        List<FhirValidationResult> libraryFhirValidationErrors = new ArrayList<>();
    }
}
