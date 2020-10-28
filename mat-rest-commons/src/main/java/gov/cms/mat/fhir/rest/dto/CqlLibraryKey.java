package gov.cms.mat.fhir.rest.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CqlLibraryKey {
    private static final String DELIMITER = "===";
    private String name;
    private String version;

    public static CqlLibraryKey fromKey(String keyIn) {
        String key = keyIn.replace("$", ".");

        String[] data = key.split(DELIMITER);

        if (data.length != 2) {
            throw new IllegalArgumentException("Invalid key: " + keyIn);
        } else {
            return CqlLibraryKey.builder()
                    .name(data[0])
                    .version(data[1])
                    .build();
        }
    }

    public String generateKey() {
        String key = name + DELIMITER + version;
        return key.replace(".", "$");
    }
}
