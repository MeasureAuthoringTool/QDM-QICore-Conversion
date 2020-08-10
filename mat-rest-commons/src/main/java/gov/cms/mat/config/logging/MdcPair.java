package gov.cms.mat.config.logging;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MdcPair {
    private final String name;
    private final String value;
}
