package gov.cms.mat.fhir.services.translate;

import gov.cms.mat.fhir.services.exceptions.HumanReadableInvalidException;
import gov.cms.mat.fhir.services.translate.creators.FhirCreator;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Narrative;
import org.springframework.beans.factory.annotation.Value;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

public abstract class TranslatorBase implements FhirCreator {
    public static final String FHIR_UNKNOWN =  "Unknown";

    @Value("${fhir.r4.public-url}")
    protected String publicHapiFhirUrl;

    @Value("${fhir.r4.baseurl}")
    protected String internalHapiFhirUrl;

    public static final String BODY_START = "<body>";
    public static final String BODY_END = "</body>";
    public static final String DIV_START = "<div>";
    public static final String DIV_END = "</div>";

    protected String buildHumanReadableDiv(String html) {
        int bodyStartIndex = html.indexOf(BODY_START);
        int bodyEndIndex = html.indexOf(BODY_END);

        if (bodyStartIndex > -1 && bodyEndIndex > -1) {
            return DIV_START + "\n" +
                    html.substring(bodyStartIndex + BODY_START.length(), bodyEndIndex).trim() +
                    "\n" + DIV_END;
        } else {
            return html;
        }
    }

    protected  Narrative createNarrative(String id, byte[] humanReadableBytes) {
        try {
            Narrative narrative = new Narrative();
            narrative.setStatusAsString("generated");
            String humanReadable = new String(humanReadableBytes, StandardCharsets.UTF_8);
            narrative.setDivAsString(buildHumanReadableDiv(humanReadable));
            return narrative;
        } catch (Exception e) {
            throw new HumanReadableInvalidException(id, new String(humanReadableBytes, StandardCharsets.UTF_8), e);
        }
    }

    protected List<CodeableConcept> createTopic() {
        return Collections.singletonList(createType("http://terminology.hl7.org/CodeSystem/definition-topic",
                "assessment"));
    }
}
