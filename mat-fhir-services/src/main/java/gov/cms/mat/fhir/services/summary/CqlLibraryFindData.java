package gov.cms.mat.fhir.services.summary;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Builder
@Getter
public class CqlLibraryFindData {
    String qdmVersion;
    String name;
    BigDecimal version;
}
