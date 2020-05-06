package gov.cms.mat.fhir.services.exceptions.cql;

import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Library;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
@Slf4j
public class LibraryAttachmentNotFoundException extends RuntimeException {
    private static final String NOT_FOUND_BY_TYPE = "Cannot find attachment type %s " +
            "for library name: %s version: %s";

    private static final String NONE_FOUND = "Cannot find any attachment for library name: %s version: %s";

    public LibraryAttachmentNotFoundException(Library library, String type) {
        super(String.format(NOT_FOUND_BY_TYPE, type, library.getName(), library.getVersion()));
        log.warn(getMessage());
    }

    public LibraryAttachmentNotFoundException(Library library) {
        super(String.format(NONE_FOUND, library.getName(), library.getVersion()));
        log.warn(getMessage());
    }
}
