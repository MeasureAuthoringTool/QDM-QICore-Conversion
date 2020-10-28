package gov.cms.mat.qdmqicore.mapping.utils;

import gov.cms.mat.qdmqicore.mapping.model.google.Cell;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SpreadSheetUtils {
    private SpreadSheetUtils() {
    }

    public static String getData(Cell cell) {
        return cell == null ? null : cell.getData();
    }

    public static List<String> commaDelimitedStringToList(Cell cell) {
        List<String> result = new ArrayList<>();
        String values = getData(cell);
        if (StringUtils.isNotBlank(values)) {
            String[] split = values.split(",");
            result = Arrays.stream(split).map(s -> StringUtils.trim(s)).collect(Collectors.toList());
        }
        return result;
    }
}
