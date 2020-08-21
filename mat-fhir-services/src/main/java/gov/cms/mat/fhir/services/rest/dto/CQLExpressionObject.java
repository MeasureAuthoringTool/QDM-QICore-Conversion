package gov.cms.mat.fhir.services.rest.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class CQLExpressionObject {

    private String type;

    private String name;

    private String logic;

    private String returnType;

    private List<String> usedExpressions = new ArrayList<>();

    private List<String> usedFunctions = new ArrayList<>();

    private List<String> usedValuesets = new ArrayList<>();

    private List<String> usedParameters = new ArrayList<>();

    private List<String> usedCodeSystems = new ArrayList<>();

    private List<String> usedCodes = new ArrayList<>();

    private Map<String, List<String>> valueSetDataTypeMap = new HashMap<>();

    private List<CQLExpressionOprandObject> oprandList = new ArrayList<CQLExpressionOprandObject>();

    private Map<String, List<String>> codeDataTypeMap = new HashMap<>();

    public CQLExpressionObject(String type, String name) {
        this.type = type;
        this.name = name;
    }
}
