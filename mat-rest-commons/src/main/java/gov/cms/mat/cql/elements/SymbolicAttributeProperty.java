package gov.cms.mat.cql.elements;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
@EqualsAndHashCode
public class SymbolicAttributeProperty {
    private String using;

    public String getMatAttributeName() {
        return using.substring(using.indexOf('.') + 1);
    }

    public String getSymbolicName() {
        return using.substring(0, using.indexOf('.'));
    }
}
