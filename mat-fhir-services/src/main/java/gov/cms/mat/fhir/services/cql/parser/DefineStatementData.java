package gov.cms.mat.fhir.services.cql.parser;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Builder
@Getter
@EqualsAndHashCode
public class DefineStatementData {

    String qdmType;
    String fhirType;
    String code;
    String alias;
    String whereAdjustment;

    @Setter
    Boolean hasWhereClause;

    Set<UnionData> unions;

    boolean isNegation() {
        return StringUtils.contains(qdmType, " Not ");
    }

    void createUnionDataSet(Collection<DefineStatementData> defineStatementDataUnions) {
        unions = new HashSet<>();

        defineStatementDataUnions.stream()
                .map(d -> UnionData.builder().fhirType(d.getFhirType()).qdmType(d.getQdmType()).build())
                .forEach(u -> unions.add(u));
    }

    @Builder
    @Getter
    @EqualsAndHashCode
    static class UnionData {
        String fhirType;
        String qdmType;
    }
}
