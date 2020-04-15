package gov.cms.mat.fhir.services.rest.support;

import gov.cms.mat.fhir.services.exceptions.InvalidVersionException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;

public interface CqlVersionConverter {
    default BigDecimal convertVersionToBigDecimal(String versionString) {
        if (StringUtils.isEmpty(versionString)) {
            throw new InvalidVersionException("Cannot find version");
        }

        long validChars = versionString.chars()
                .filter(ch -> Character.isDigit(ch) || ch == '.')
                .count();

        if (versionString.length() != validChars) {
            throw new InvalidVersionException("Version can contain only numbers and two decimal points: " + versionString);
        }

        long count = versionString.chars()
                .filter(ch -> ch == '.')
                .count();

        if (count != 2) {
            throw new InvalidVersionException("Version is invalid: " + versionString);
        }

        int firstDecimalPointPointer = versionString.indexOf('.');

        String majorVersion = versionString.substring(0, firstDecimalPointPointer);

        int secondDecimalPointPointer = versionString.indexOf('.', firstDecimalPointPointer + 1);

        String minorVersion = versionString.substring(firstDecimalPointPointer + 1, secondDecimalPointPointer);

        String zeroPadding = getZeroPaddingAndDecimal(minorVersion.length());

        String convertedValue = majorVersion + zeroPadding + minorVersion;

        return new BigDecimal(convertedValue);
    }

    default String getZeroPaddingAndDecimal(int length) {
        if (length > 2) {
            return ".";
        } else if (length == 2) {
            return ".0";
        } else {
            return ".00";
        }
    }


    default Pair<BigDecimal, Integer> versionToVersionAndRevision(String version) {
        if (isValidVersion(version)) {
            String[] sp = version.split("\\.");
            return Pair.of(new BigDecimal(sp[0] + "." + sp[1]), Integer.parseInt(sp[2]));
        } else {
            throw new InvalidVersionException("Invalid version: " + version);
        }
    }

    private boolean isValidVersion(String version) {
        boolean result = true;
        String[] parts = version.split("\\.");
        if (parts.length == 3) {
            for (String p : parts) {
                try {
                    Integer.parseInt(p);
                } catch (NumberFormatException nfe) {
                    result = false;
                    break;
                }
            }
            result = result && parts[2].length() == 3;
        } else {
            result = false;
        }
        return result;
    }


}
