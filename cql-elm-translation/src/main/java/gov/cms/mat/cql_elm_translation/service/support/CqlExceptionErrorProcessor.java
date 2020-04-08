package gov.cms.mat.cql_elm_translation.service.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.fhir.rest.dto.MatCqlConversionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cqframework.cql.cql2elm.CqlTranslatorException;
import org.cqframework.cql.elm.tracking.TrackBack;
import org.hl7.cql_annotations.r1.CqlToElmError;
import org.hl7.elm.r1.Library;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class CqlExceptionErrorProcessor {
    private static final ObjectMapper mapper = new ObjectMapper();

    private final List<CqlTranslatorException> cqlErrors;
    private final String json;
    private final Library library;

    public CqlExceptionErrorProcessor(List<CqlTranslatorException> cqlErrors, String json, Library library) {
        this.cqlErrors = cqlErrors;
        this.json = json;
        this.library = library;
    }

    public String process() {
        try {
            if (CollectionUtils.isEmpty(cqlErrors)) {
                return json;
            } else {
                return addErrorsToJson();
            }
        } catch (Exception e) {
            log.error("Cannot parse json.", e);
            log.trace(json);
            return json;
        }
    }

    private String addErrorsToJson() throws JsonProcessingException {
        mapper.readTree(json);

        List<MatCqlConversionException> matErrors = buildMatErrors();
        String jsonToInsert = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(matErrors);

        String temp = json.replaceFirst("\n", "\n  \"errorExceptions\":" + jsonToInsert + ",\n");

        return temp;
    }

    private String escape(String raw) {
        String escaped = raw;
        escaped = escaped.replace("\\", "\\\\");
        escaped = escaped.replace("\"", "\\\"");
        escaped = escaped.replace("\b", "\\b");
        escaped = escaped.replace("\f", "\\f");
        escaped = escaped.replace("\n", "\\n");
        escaped = escaped.replace("\r", "\\r");
        escaped = escaped.replace("\t", "\\t");
        // TODO: escape other non-printing characters using uXXXX notation
        return escaped;
    }

    private List<MatCqlConversionException> buildMatErrors() {
        return cqlErrors.stream()
                //.filter(this::filterByMessage)
                .map(this::createDto)
                .collect(Collectors.toList());
    }

    private boolean filterByMessage(CqlTranslatorException cqlTranslatorException) {
        List<Object> objectList = library.getAnnotation(); //todo revisit

        if (CollectionUtils.isEmpty(objectList)) {
            return true;
        } else {
            var optional = objectList.stream()
                    .filter(o -> o instanceof CqlToElmError)
                    .map(CqlToElmError.class::cast)
                    .filter(m -> m.getMessage() != null)
                    .map(CqlToElmError::getMessage)
                    .filter(m -> m.equals(cqlTranslatorException.getMessage()))
                    .findFirst();

            return optional.isEmpty(); // did not find add to list
        }
    }


    private MatCqlConversionException createDto(CqlTranslatorException cqlException) {
        MatCqlConversionException matCqlConversionException = buildMatError(cqlException);

        if (cqlException.getLocator() == null) {
            log.warn("Locator is null");
        } else {
            addLocatorData(cqlException.getLocator(), matCqlConversionException);
        }

        return matCqlConversionException;
    }

    private void addLocatorData(TrackBack locator, MatCqlConversionException matCqlConversionException) {
        matCqlConversionException.setStartLine(locator.getStartLine());
        matCqlConversionException.setStartChar(locator.getStartChar());
        matCqlConversionException.setEndLine(locator.getEndLine());
        matCqlConversionException.setEndChar(locator.getEndChar());
        matCqlConversionException.setTargetIncludeLibraryVersionId(locator.getLibrary().getVersion());
        matCqlConversionException.setTargetIncludeLibraryId(locator.getLibrary().getId());
    }

    private MatCqlConversionException buildMatError(CqlTranslatorException c) {
        MatCqlConversionException matCqlConversionException = new MatCqlConversionException();
        matCqlConversionException.setErrorSeverity(c.getSeverity().name());


        try {
            String payload = escape(c.getMessage());
            matCqlConversionException.setMessage(payload);
        } catch (Exception e) {
            log.debug("Error building MatError", e);
            matCqlConversionException.setMessage("Exception");
        }

        return matCqlConversionException;
    }

}
