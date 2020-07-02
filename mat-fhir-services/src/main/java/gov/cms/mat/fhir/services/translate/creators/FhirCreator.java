package gov.cms.mat.fhir.services.translate.creators;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import gov.cms.mat.cql.elements.LibraryProperties;
import org.hl7.fhir.r4.model.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;

public interface FhirCreator {

    default CodeableConcept createType(String type, String code) {
        return new CodeableConcept()
                .setCoding(Collections.singletonList(new Coding(type, code, null)));
    }

    /* rawData are bytes that are NOT base64 encoded */
    default Attachment createAttachment(String contentType, byte[] rawData) {
        return new Attachment()
                .setContentType(contentType)
                .setData(rawData == null ? null : encodeBase64(rawData));
    }

    default byte[] encodeBase64(byte[] src) {
        return Base64.getEncoder().encode(src);
    }


    default Identifier createIdentifier(String system, String value) {
        return new Identifier()
                .setSystem(system)
                .setValue(value);
    }

    default CodeableConcept buildCodeableConcept(String code, String system, String display) {
        CodeableConcept codeableConcept = new CodeableConcept();
        codeableConcept.setCoding(new ArrayList<>());
        codeableConcept.getCoding()
                .add(buildCoding(code, system, display));
        return codeableConcept;
    }

    default Coding buildCoding(String code, String system, String display) {
        return new Coding()
                .setCode(code)
                .setSystem(system)
                .setDisplay(display);
    }

    default Period buildPeriodDayResolution(Date startDate, Date endDate) {
        return new Period()
                .setStart(startDate, TemporalPrecisionEnum.DAY)
                .setEnd(endDate, TemporalPrecisionEnum.DAY);
    }

    default String createLibraryUuid(LibraryProperties properties) {
        return createLibraryUuid(properties.getName(), properties.getVersion());
    }

    default String createLibraryUuid(String name, String version) {
        return name.replace('_', '-') + "-" + version.replace('.', '-');
    }

    default String createVersion(BigDecimal version, Integer revision) {
        String[] parts = version.toString().split("\\.");

        if (parts.length != 2) {
            throw new IllegalArgumentException("Cannot parse version: " + version);
        } else {
            return parts[0] + '.' + revision + '.' + parts[1];
        }
    }
}
