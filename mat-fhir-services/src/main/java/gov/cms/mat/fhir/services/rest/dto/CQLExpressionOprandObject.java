package gov.cms.mat.fhir.services.rest.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class CQLExpressionOprandObject {

    private String name;
    private String returnType;

}
