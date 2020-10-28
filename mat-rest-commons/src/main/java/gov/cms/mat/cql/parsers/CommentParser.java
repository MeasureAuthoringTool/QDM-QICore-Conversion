package gov.cms.mat.cql.parsers;

import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.atomic.AtomicBoolean;

interface CommentParser {
    default boolean lineComment(String line, AtomicBoolean isInComment) {
        if (isInComment.get()) {
            return lineCommentWhileInComment(line, isInComment);
        } else {
            return lineCommentWhileNotInComment(line, isInComment);
        }
    }

    private boolean lineCommentWhileNotInComment(String line, AtomicBoolean isInComment) {
        if (line.trim().startsWith("//")) {
            return true;
        } else if (line.trim().startsWith("/*")) {

            if (!line.contains("*/")) {
                isInComment.set(true);
            }

            return true;
        } else {
            return false;
        }
    }

    private boolean lineCommentWhileInComment(String line, AtomicBoolean isInComment) {
        if (line.contains("*/")) {
            isInComment.set(false);
            return false;
        } else {
            return true;
        }
    }

    default String getCommentAtEnd(String line) {
        int index = StringUtils.lastIndexOf(line, "'");

        if (index < 0) {
            return null;
        } else {
            if (index + 1 >= line.length()) {
                return null;
            } else {
                return line.substring(index + 1);
            }
        }
    }
}
