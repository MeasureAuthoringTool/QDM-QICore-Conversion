package gov.cms.mat.fhir.services.rest.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class CQLObject {
    private List<CQLExpressionObject> cqlDefinitionObjectList = new ArrayList<>();
    private List<CQLExpressionObject> cqlFunctionObjectList = new ArrayList<>();
    private List<CQLExpressionObject> cqlParameterObjectList = new ArrayList<>();
}
