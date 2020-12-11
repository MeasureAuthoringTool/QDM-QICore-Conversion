package gov.cms.mat.fhir.commons.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class HumanReadableArtifacts {
    private Set<HumanReadableCodeModel> terminologyCodeModels = new HashSet<>();
    private Set<HumanReadableValuesetModel> terminologyValueSetModels = new HashSet<>();
    private Set<HumanReadableCodeModel> dataReqCodes = new HashSet<>();
    private Set<HumanReadableTypeModel> dataReqTypes = new HashSet<>();
    private Set<HumanReadableValuesetModel> dataReqValueSets = new HashSet<>();
}
