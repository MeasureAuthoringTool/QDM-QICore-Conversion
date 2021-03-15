package gov.cms.mat.fhir.services.cql.parser;

import mat.shared.CQLError;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Visitor pattern for parsing cql. The parsing uses a callback pattern where corresponding
 * methods are invoked when that section is encountered in the cql.
 */
public interface CqlVisitor {
    default void validateBeforeParse() {
        // by default do nothing unless its implemented.
    }

    default void validateAfterParse() {
        // by default do nothing unless its implemented.
    }

    default void handleError(CQLError error) {
        // by default do nothing unless its implemented.
    }

    default boolean isRemovingBlockComments() {
        return false;
    }

    default boolean isRemovingLineComments() {
        return false;
    }

    default void libraryTag(String libraryName,
                            String version,
                            @Nullable String libraryComment,
                            int lineNumber) {

        System.out.println("here");
        // by default do nothing unless its implemented.
    }

    default void usingModelVersionTag(String model, String fhirVersion, int lineNumber) {
        // by default do nothing unless its implemented.
    }

    default void includeLib(String libName,
                            String version,
                            String alias,
                            String model,
                            String modelVersion,
                            int lineNumber) {
        // by default do nothing unless its implemented.
    }

    default void codeSystem(String name,
                            String uri,
                            String versionUri,
                            int lineNumber) {
        //by default do nothing unless its implemented.

    }

    default void valueSet(String type, String uri, int lineNumber) {
        //by default do nothing unless its implemented.
        System.out.println("Here");
    }

    default void code(String name, String code, String codeSystemName, String displayName, int lineNumber) {
        //by default do nothing unless its implemented.
    }

    default void parameter(String name, String logic, String comment, int lineNumber) {
        //by default do nothing unless its implemented.
    }

    default void context(String context, int lineNumber) {
        //by default do nothing unless its implemented.
    }

    default void definition(String name, String logic, String comment, int lineNumber) {
        //by default do nothing unless its implemented.
    }

    default void function(String name, List<FunctionArgument> args, String logic, String comment, int lineNumber) {
        //by default do nothing unless its implemented.
    }
}


