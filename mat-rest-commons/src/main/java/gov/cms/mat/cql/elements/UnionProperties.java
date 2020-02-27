package gov.cms.mat.cql.elements;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UnionProperties {
    List<String> lines = new ArrayList<>();

    public void addLine(String line) {
        lines.add(line);
    }

}
