package gov.cms.mat.cql.elements;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
@EqualsAndHashCode
public class WithProperty {
    private String using;

    public String getMatAttributeName() {
        return using.substring(using.indexOf('.'));
    }
}
