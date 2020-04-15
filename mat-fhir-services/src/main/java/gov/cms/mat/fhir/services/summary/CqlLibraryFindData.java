package gov.cms.mat.fhir.services.summary;

import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;

@Builder
@Getter
public class CqlLibraryFindData {
    String qdmVersion;
    String name;
    BigDecimal matVersion;
    String version;
    String type;
    Pair<BigDecimal, Integer> pair;
}
