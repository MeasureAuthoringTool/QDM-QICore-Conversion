package gov.cms.mat.patients.conversion.dao;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class JsonDateTime {
    @JsonProperty("$date")
    Date date;
}
