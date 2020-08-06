package gov.cms.mat.config.logging;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MdcPair {
    private String name;
    private String value;
}
