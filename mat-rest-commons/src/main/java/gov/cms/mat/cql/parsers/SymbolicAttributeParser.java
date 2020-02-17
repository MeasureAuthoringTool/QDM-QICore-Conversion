package gov.cms.mat.cql.parsers;

import gov.cms.mat.cql.elements.SymbolicAttributeProperty;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

@Slf4j
public class SymbolicAttributeParser {
    private final String defineData;
    String searchKey;
    Set<SymbolicAttributeProperty> set;

    public SymbolicAttributeParser(String defineData) {
        this.defineData = defineData;
    }

    public Set<SymbolicAttributeProperty> find(String symbolic) {
        searchKey = symbolic + '.';
        set = new HashSet<>();

        process();

        return set;
    }

    private void process() {
        int fromIndex = 0;

        while (fromIndex < defineData.length()) {
            int start = defineData.indexOf(searchKey, fromIndex);

            if (start == -1) {
                break;
            }

            int end = defineData.indexOf(' ', start);

            if (end == -1) {
                end = defineData.length();
            }

            String attribute = defineData.substring(start, end);

            if (attribute.endsWith(",")) {
                attribute = attribute.substring(0, attribute.length() - 1);
            }

            set.add(buildSymbolicAttributeProperty(attribute));
            fromIndex += attribute.length();
        }
    }

    private SymbolicAttributeProperty buildSymbolicAttributeProperty(String attribute) {
        return SymbolicAttributeProperty.builder()
                .using(attribute)
                .build();
    }
}
