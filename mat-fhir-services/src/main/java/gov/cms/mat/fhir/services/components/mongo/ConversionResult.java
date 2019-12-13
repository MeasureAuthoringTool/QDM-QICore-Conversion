package gov.cms.mat.fhir.services.components.mongo;

import gov.cms.mat.fhir.services.service.support.CqlConversionError;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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

    private List<FieldConversionResult> measureResults = new ArrayList<>();

    private List<FieldConversionResult> libraryResults = new ArrayList<>();
    private ConversionType libraryConversionType;

    private CqlConversionResult cqlConversionResult;


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
        List<CqlConversionError> cqlConversionErrors;
    }
}
