package gov.cms.mat.fhir.commons.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HumanReadableCodeModel {
	private String name;
	private String oid;
	private String codesystemName;
	private String codesystemVersion;
	private boolean isCodesystemVersionIncluded;
	private String datatype;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		HumanReadableCodeModel that = (HumanReadableCodeModel) o;

		if (!name.equals(that.name)) return false;
		if (!oid.equals(that.oid)) return false;
		if (!codesystemName.equals(that.codesystemName)) return false;
		return codesystemVersion.equals(that.codesystemVersion);
	}

	@Override
	public int hashCode() {
		int result = name.hashCode();
		result = 31 * result + oid.hashCode();
		result = 31 * result + codesystemName.hashCode();
		if (codesystemVersion != null) {
			result = 31 * result + codesystemVersion.hashCode();
		}
		return result;
	}
}




