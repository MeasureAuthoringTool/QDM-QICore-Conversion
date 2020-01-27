package gov.cms.mat.fhir.services.summary;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

@Data
public class BatchResult {
    List<String> ids = new ArrayList<>();
    double averageSeconds;
    Integer count;

    @JsonIgnore
    List<Long> times = new ArrayList<>();

    public void compute() {
        OptionalDouble optionalDouble = times.stream()
                .mapToLong(Long::longValue)
                .average();

        averageSeconds = optionalDouble.orElse(-1.0);

        if (averageSeconds != -1) {
            averageSeconds = averageSeconds / 1000;
        }

        count = ids.size();
    }
}
