package mat.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class CQLError implements IsSerializable, Comparable<CQLError> {
	private int errorInLine;
	private int errorAtOffset;
	private int startErrorInLine;
	private int startErrorAtOffset;
	private int endErrorInLine;
	private int endErrorAtOffset;

	private String errorMessage;
	private String severity;

	@Override
    public int compareTo(CQLError o) {
           if(this.errorInLine > o.errorInLine) {
                  return 1;
           }else if(this.errorInLine < o.errorInLine) {
                  return -1;
           }
           return 0;
    }
}
