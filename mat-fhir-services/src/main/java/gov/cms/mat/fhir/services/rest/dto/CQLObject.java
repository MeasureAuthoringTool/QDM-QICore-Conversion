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
    List<CQLExpressionObject> cqlDefinitionObjectList = new ArrayList<>();
    List<CQLExpressionObject> cqlFunctionObjectList = new ArrayList<>();
    List<CQLExpressionObject> cqlParameterObjectList = new ArrayList<>();
}
