package gov.cms.mat.cql_elm_translation.service.filters;

import gov.cms.mat.cql.elements.LibraryProperties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cqframework.cql.cql2elm.CqlTranslatorException;
import org.hl7.elm.r1.VersionedIdentifier;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class CqlTranslatorExceptionFilter implements CqlLibraryFinder {
    @Getter
    private final String cqlData;
    private final boolean showWarnings;
    private final List<CqlTranslatorException> exceptions;

    public CqlTranslatorExceptionFilter(String cqlData,
                                        boolean showWarnings,
                                        List<CqlTranslatorException> exceptions) {
        this.cqlData = cqlData;
        this.showWarnings = showWarnings;
        this.exceptions = exceptions;
    }

    public List<CqlTranslatorException> filter() {
        if (CollectionUtils.isEmpty(exceptions)) {
            return Collections.emptyList();
        } else {
            List<CqlTranslatorException> warningsRemovedErrors = filterOutWarnings();

            if (warningsRemovedErrors.isEmpty()) {
                return Collections.emptyList();
            } else {
                return filterByLibrary(warningsRemovedErrors);
            }
        }
    }

    private List<CqlTranslatorException> filterByLibrary(List<CqlTranslatorException> errors) {
        LibraryProperties libraryProperties = parseLibrary();

        return errors.stream()
                .filter(e -> filterOutInclude(e, libraryProperties))
                .collect(Collectors.toList());
    }

    private boolean filterOutInclude(CqlTranslatorException translatorException, LibraryProperties libraryProperties) {
        if (translatorException.getLocator() == null || translatorException.getLocator().getLibrary() == null) {
            return false;
        } else {
            VersionedIdentifier versionedIdentifier = translatorException.getLocator().getLibrary();
            log.debug("versionedIdentifier : {}", versionedIdentifier);
            return isPointingToSameLibrary(libraryProperties, versionedIdentifier);
        }
    }

    private boolean isPointingToSameLibrary(LibraryProperties p, VersionedIdentifier v) {
        return p.getName().equals(v.getId()) && p.getVersion().equals(v.getVersion());
    }


    private List<CqlTranslatorException> filterOutWarnings() {
        List<CqlTranslatorException> warningsRemovedList;

        if (showWarnings) {
            warningsRemovedList = exceptions;
        } else {
            warningsRemovedList = exceptions.stream()
                    .filter(this::isError)
                    .collect(Collectors.toList());
        }

        return warningsRemovedList;
    }

    private boolean isError(CqlTranslatorException e) {
        return e != null && e.getSeverity() != null &&
                e.getSeverity() == CqlTranslatorException.ErrorSeverity.Error;
    }
}
