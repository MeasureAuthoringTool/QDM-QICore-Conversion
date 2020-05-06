package gov.cms.mat.fhir.rest.dto.spreadsheet;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class QdmToFhirMappingHelper {
    Map<String, QdmToQicoreMapping> qdmTypeToMappings = new HashMap<>();
    Map<Pair<String, String>, QdmToQicoreMapping> qdmTypeAttributeMappings = new HashMap<>();

    public QdmToFhirMappingHelper(List<QdmToQicoreMapping> mappings) {
        mappings.stream().filter(m -> StringUtils.isNotBlank(m.getMatDataType()) &&
                StringUtils.isNotBlank(m.getFhirQICoreMapping()) &&
                m.getFhirQICoreMapping().contains(".")).forEach(m -> {
            String qdmType = m.getMatDataType();
            String qdmAttrib = m.getMatAttributeType();
            qdmTypeToMappings.put(qdmType, m);
            if (StringUtils.isNotBlank(qdmAttrib)) {
                String[] qdmAttribs = qdmAttrib.split(" ");
                Arrays.stream(qdmAttribs).forEach(a -> qdmTypeAttributeMappings.put(Pair.of(qdmType, a), m));
            }
        });
    }

    public boolean contains(String qdmType) {
        return qdmTypeToMappings.containsKey(qdmType);
    }

    public boolean contains(String qdmType, String qdmAttribute) {
        return qdmTypeAttributeMappings.containsKey(Pair.of(qdmType, qdmAttribute));
    }

    public String convertType(String type) {
        String result = type;
        if (qdmTypeToMappings.containsKey(type)) {
            String fhirMapping = qdmTypeToMappings.get(type).getFhirQICoreMapping();
            //We know it will contain a . so no need to check.
            result = fhirMapping.substring(0, fhirMapping.indexOf("."));
        }
        return result;
    }

    public String convertTypeAndAttribute(String qdmType, String qdmAttribute) {
        String result;
        if (contains(qdmType, qdmAttribute)) {
            QdmToQicoreMapping m = qdmTypeAttributeMappings.get(Pair.of(qdmType, qdmAttribute));
            result = m.getFhirQICoreMapping();
        } else {
            result = (contains(qdmType) ? convertType(qdmType) : qdmType) + "." + qdmAttribute;
        }
        return result;
    }
}
