package gov.cms.mat.fhir.services.components.mongo;

import gov.cms.mat.fhir.rest.dto.ConversionType;
import gov.cms.mat.fhir.rest.dto.LibraryConversionResults;
import gov.cms.mat.fhir.rest.dto.MeasureConversionResults;
import gov.cms.mat.fhir.rest.dto.ValueSetConversionResults;
import gov.cms.mat.fhir.services.exceptions.CqlLibraryNotFoundException;
import gov.cms.mat.fhir.services.exceptions.ValueSetNotFoundException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Optional;

@Document
@Data
@Slf4j
public class ConversionResult {
    //  ValueSetConversionResults valueSetConversionResults;
    MeasureConversionResults measureConversionResults;


    List<ValueSetConversionResults> valueSetConversionResults = new ArrayList<>();
    List<LibraryConversionResults> libraryConversionResults = new ArrayList<>();

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

    private String errorReason;

    private ConversionType conversionType;

    private Optional<LibraryConversionResults> findFirstLibraryResult(String matLibraryId) {
        return libraryConversionResults.stream()
                .filter(l -> l.getMatId().equals(matLibraryId))
                .findFirst();
    }

    private Optional<ValueSetConversionResults> findFirstValueSetResult(String oid) {
        return valueSetConversionResults.stream()
                .filter(l -> l.getOid().equals(oid))
                .findFirst();
    }

    private ValueSetConversionResults createValueSetResult(String oid) {
        log.debug("Creating new ValueSetConversionResults: {}", oid);

        ValueSetConversionResults createdResults = new ValueSetConversionResults(oid);

        valueSetConversionResults.add(createdResults);

        return createdResults;
    }

    public ValueSetConversionResults findOrCreateValueSetConversionResults(String oid) {
        return findFirstValueSetResult(oid)
                .orElseGet(() -> createValueSetResult(oid));
    }

    public ValueSetConversionResults findValueSetConversionResultsRequired(String oid) {
        return findFirstValueSetResult(oid)
                .orElseThrow(() -> new ValueSetNotFoundException(oid));
    }


    private LibraryConversionResults createLibraryResult(String matLibraryId) {
        log.debug("Creating new LibraryConversionResults: {}", matLibraryId);

        LibraryConversionResults createdResults = new LibraryConversionResults(matLibraryId);

        libraryConversionResults.add(createdResults);

        return createdResults;
    }

    public LibraryConversionResults findLibraryConversionResultsRequired(String matLibraryId) {
        return findFirstLibraryResult(matLibraryId)
                .orElseThrow(() -> new CqlLibraryNotFoundException(measureId));
    }

    public LibraryConversionResults findOrCreateLibraryConversionResults(String matLibraryId) {
        return findFirstLibraryResult(matLibraryId)
                .orElseGet(() -> createLibraryResult(matLibraryId));
    }
}
