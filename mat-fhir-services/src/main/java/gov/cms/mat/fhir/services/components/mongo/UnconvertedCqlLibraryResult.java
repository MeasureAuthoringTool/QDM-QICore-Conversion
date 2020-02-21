package gov.cms.mat.fhir.services.components.mongo;

import gov.cms.mat.fhir.services.summary.CqlLibraryFindData;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document
@Data
@Slf4j
public class UnconvertedCqlLibraryResult {
    @Id
    private String id;
    @CreatedDate
    private Instant created;
    @Indexed(unique = true)
    private String name;

    private CqlLibraryFindData findData;

    private String cql;
}
