package gov.cms.mat.cql.parsers;

import gov.cms.mat.cql.elements.SymbolicProperty;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public interface SymbolicParser {

    default List<SymbolicProperty> getSymbolicProperties(String allLines) {
        String[] lines = allLines.split("\\r?\\n");

        return getSymbolicProperties(lines);
    }

    private List<SymbolicProperty> getSymbolicProperties(String[] lines) {
        return Arrays.stream(lines)
                .filter(l -> l.contains("[\""))
                .map(this::buildSymbolicProperties)
                .collect(Collectors.toList());
    }

    default List<SymbolicProperty> getSymbolicProperties(List<String> linesIn) {
        String[] lines = linesIn.stream().toArray(String[]::new);

        return getSymbolicProperties(lines);
    }

    private SymbolicProperty buildSymbolicProperties(String line) {
        // ["Condition": "Heart Failure"] HeartFailure
        return SymbolicProperty.builder()
                .matDataTypeDescription(findMatDataTypeDescription(line))
                .symbolic(findSymbolic(line))
                .build();
    }

    private String findSymbolic(String line) {
        //  ["Medication, Order": "Tobacco Use Cessation Pharmacotherapy"] ) User

        String symbolic = StringUtils.substringAfter(line, "\"]");

        if (StringUtils.isBlank(symbolic)) {
            return null;
        } else {
            symbolic = symbolic.trim().replaceAll("[^a-zA-Z0-9]", "");

            if (StringUtils.isEmpty(symbolic)) {
                return null;
            } else {
                return symbolic;
            }
        }
    }

    private String findMatDataTypeDescription(String line) {
        return StringUtils.substringBetween(line, "[\"", "\":");
    }
}
