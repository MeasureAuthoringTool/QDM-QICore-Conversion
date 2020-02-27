package gov.cms.mat.cql.parsers;

import gov.cms.mat.cql.elements.UnionProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public interface UnionParser {
    String[] getLines();

    default List<UnionProperties> getUnions() {
        List<UnionProperties> properties = new ArrayList<>();
        Iterator<String> iterator = Arrays.stream(getLines()).iterator();

        while (iterator.hasNext()) {
            String line = iterator.next().trim();

            if (isUnion(line)) {
                properties.add(buildUnionProperties(line, iterator));
            }
        }

        return properties;
    }

    private UnionProperties buildUnionProperties(String firstLine, Iterator<String> iterator) {
        UnionProperties unionProperties = new UnionProperties();
        unionProperties.addLine(firstLine);

        while (iterator.hasNext()) {
            String line = iterator.next().trim();

            if (isUnion(line)) {
                unionProperties.addLine(line);
            } else {
                break;
            }
        }

        return unionProperties;
    }

    private boolean isUnion(String line) {
        return line.startsWith("union ");
    }


}
