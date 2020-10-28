package mat.client.measurepackage;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AggregateFunction {
    private String display;
    private CQLFunction cqlFunction;
}
