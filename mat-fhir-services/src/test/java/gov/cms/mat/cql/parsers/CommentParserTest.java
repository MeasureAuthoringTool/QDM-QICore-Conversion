package gov.cms.mat.cql.parsers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class CommentParserTest {

   static class CommentParserImpl implements CommentParser {

    }

    CommentParserImpl commentParser;

    @BeforeEach
    void setUp() {
        commentParser = new CommentParserImpl();
    }


    @Test
    void lineNotCommentCheckWhackWhack() {
        String line = "// i am whack whack comment";

        assertTrue(commentParser.lineComment(line, new AtomicBoolean(false)));
    }

    @Test
    void lineNotCommentCheckMulitLineNotClosed() {
        String line = "/* i am a multi-line not closed";

        AtomicBoolean isInComment = new AtomicBoolean(false);

        assertTrue(commentParser.lineComment(line, isInComment));

        assertTrue(isInComment.get());
    }


    @Test
    void isInCommentNOTClosed() {
        String line = "// i am  not closed";

        AtomicBoolean isInComment = new AtomicBoolean(true);

        assertTrue(commentParser.lineComment(line, isInComment));

        assertTrue(isInComment.get());
    }

    @Test
    void isInCommentClosed() {
        String line = "i am Closed */";

        AtomicBoolean isInComment = new AtomicBoolean(true);

        assertFalse(commentParser.lineComment(line, isInComment));

        assertFalse(isInComment.get());
    }

    @Test
    void lineNotCommentCheckMulitLineClosed() {
        String line = "/* i am a multi-line CLOSED */";

        AtomicBoolean isInComment = new AtomicBoolean(false);

        assertTrue(commentParser.lineComment(line, isInComment));

        assertFalse(isInComment.get());
    }

    @Test
    void getCommentAtEnd() {
        String line = "library MATGlobalCommonFunctions version '1.0.000' // this best lib ever";

        String comment = commentParser.getCommentAtEnd(line);

        assertEquals(" // this best lib ever", comment);
    }

    @Test
    void getCommentAtEndNoComment() {
        String line = "library MATGlobalCommonFunctions version '1.0.000'";

        assertNull(commentParser.getCommentAtEnd(line));
    }

    @Test
    void getCommentAtEndBadData() {
        String line = "Bad data";

        assertNull(commentParser.getCommentAtEnd(line));
    }
}