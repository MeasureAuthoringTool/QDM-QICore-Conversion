package gov.cms.mat.cql.parsers;

import gov.cms.mat.cql.elements.UnionProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public interface UnionParser extends CommentParser {
    String[] getLines();

    default List<UnionProperties> getUnions() {
        List<UnionProperties> properties = new ArrayList<>();
        Iterator<String> iterator = Arrays.stream(getLines()).iterator();
        AtomicBoolean isInComment = new AtomicBoolean(false);

        while (iterator.hasNext()) {
            String line = iterator.next().trim();

            if (!lineComment(line, isInComment)) {
                if (isUnion(line)) {
                    properties.add(buildUnionProperties(line, iterator));
                }
            }
        }

        return properties;
    }

    private UnionProperties buildUnionProperties(String firstLine, Iterator<String> iterator) {
        UnionProperties unionProperties = new UnionProperties();
        unionProperties.addLine(firstLine);

        AtomicBoolean isInComment = new AtomicBoolean(false);

        while (iterator.hasNext()) {
            String line = iterator.next().trim();

            if (lineComment(line, isInComment)) {
                continue;
            }

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
