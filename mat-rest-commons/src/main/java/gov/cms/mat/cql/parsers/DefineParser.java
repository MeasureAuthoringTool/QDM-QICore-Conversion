package gov.cms.mat.cql.parsers;

import gov.cms.mat.cql.elements.DefineProperties;
import gov.cms.mat.cql.elements.SymbolicAttributeProperty;
import gov.cms.mat.cql.elements.SymbolicProperty;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public interface DefineParser extends SymbolicParser, CommentParser {
    String[] getLines();

    default List<DefineProperties> getDefines() {
        List<DefineProperties> properties = new ArrayList<>();
        Iterator<String> iterator = Arrays.stream(getLines()).iterator();
        AtomicBoolean isInComment = new AtomicBoolean(false);

        while (iterator.hasNext()) {
            String line = iterator.next();

            if (!lineComment(line, isInComment)) {
                if (line.startsWith("define ")) {
                    properties.add(buildDefineProperties(line, iterator));
                }
            }
        }

        return properties;
    }

    private DefineProperties buildDefineProperties(String firstLine, Iterator<String> iterator) {
        StringBuilder stringBuilder = new StringBuilder(firstLine);
        stringBuilder.append("\n");

        while (iterator.hasNext()) {
            String line = iterator.next();

            if (line == null || line.trim().length() == 0) {
                break;
            } else {
                stringBuilder.append(line).append("\n");
            }
        }

        List<SymbolicProperty> symbolicProperties = getSymbolicProperties(stringBuilder.toString());

        symbolicProperties.stream()
                .filter(s -> StringUtils.isNotEmpty(s.getSymbolic()))
                .forEach(p -> parseSymbolicAttributes(p, stringBuilder.toString()));

        return DefineProperties.builder()
                .line(stringBuilder.toString())
                .defineData(stringBuilder.toString())
                .symbolicProperties(symbolicProperties)
                .build();
    }


    private void parseSymbolicAttributes(SymbolicProperty symbolic, String defineData) {
        SymbolicAttributeParser symbolicAttributeParser = new SymbolicAttributeParser(defineData);
        Set<SymbolicAttributeProperty> set = symbolicAttributeParser.find(symbolic.getSymbolic());
        symbolic.setAttributePropertySet(set);
    }
}
