package net.jbock.util;

import io.jbock.util.Either;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

import static net.jbock.util.ParseRequestExpand.LineResult.BACKSLASH_BEFORE_EOF;
import static net.jbock.util.ParseRequestExpand.LineResult.UNMATCHED_QUOTE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParseRequestExpandTest {

    @Test
    void testAtFileSyntax() {
        List<String> lines = List.of(
                "",
                "1",
                "",
                "2\\\"\\ \\\\3\\",
                "  4 ",
                "",
                "",
                "",
                "");
        List<String> tokens = read(lines);
        assertEquals(List.of(
                "1",
                "2\" \\3  4 "),
                tokens);
    }

    @Test
    void testEmpty() {
        List<String> tokens = read(List.of(""));
        assertEquals(List.of(), tokens);
    }

    @Test
    void testJoinedEmptyLines() {
        List<String> tokens = read(List.of("\\", "''\\", "\\", "\"\""));
        assertEquals(List.of(""), tokens);
    }


    @Test
    void backslashBeforeEof() {
        ParseRequestExpand.NumberedLineResult error = expectError(List.of("'a'\\"));
        assertTrue(error.lineResult().isError());
        assertEquals(1, error.number());
        assertEquals(BACKSLASH_BEFORE_EOF, error.lineResult());
    }

    @Test
    void backslashBeforeEofEscapedDouble() {
        ParseRequestExpand.NumberedLineResult error = expectError(List.of("\"\\"));
        assertEquals(1, error.number());
        assertEquals(UNMATCHED_QUOTE, error.lineResult());
    }

    @Test
    void backslashBeforeEofEscapedSingle() {
        ParseRequestExpand.NumberedLineResult error = expectError(List.of("'\\"));
        assertEquals(1, error.number());
        assertEquals(UNMATCHED_QUOTE, error.lineResult());
    }

    @Test
    void testUnmatchedSingleQuote() {
        ParseRequestExpand.NumberedLineResult error = expectError(List.of("'"));
        assertTrue(error.lineResult().isError());
        assertEquals(1, error.number());
        assertEquals("unmatched quote", error.lineResult().message());
    }

    @Test
    void testUnmatchedDoubleQuote() {
        ParseRequestExpand.NumberedLineResult error = expectError(List.of("\""));
        assertTrue(error.lineResult().isError());
        assertEquals(1, error.number());
        assertEquals("unmatched quote", error.lineResult().message());
    }

    @Test
    void testNewline() {
        List<String> tokens = read(List.of("\\n"));
        assertEquals(List.of("\n"), tokens);
    }

    @Test
    void testNewlineSingleQuotes() {
        List<String> tokens = read(List.of("'\\n'"));
        assertEquals(List.of("\\n"), tokens);
    }

    @Test
    void testNewlineDoubleQuotes() {
        List<String> tokens = read(List.of("\"\\n\""));
        assertEquals(List.of("\n"), tokens);
    }

    @Test
    void testDoubleQuotes() {
        List<String> tokens = read(List.of("\"\""));
        assertEquals(List.of(""), tokens);
    }

    @Test
    void testMixedQuotes() {
        assertEquals(List.of("\"\""), read(List.of("'\"\"'")));
        assertEquals(List.of("''"), read(List.of("\"''\"")));
    }

    @Test
    void testSingleQuotesEmpty() {
        assertEquals(List.of(""), read(List.of("''")));
    }

    @Test
    void testSingleBackslashInSingleQuotes() {
        assertEquals(List.of("\\"), read(List.of("'\\'")));
    }

    @Test
    void testSingleBackslashInDoubleQuotes() {
        ParseRequestExpand.NumberedLineResult error = expectError(List.of("\"\\\""));
        assertTrue(error.lineResult().isError());
        assertEquals(1, error.number());
        assertEquals("unmatched quote", error.lineResult().message());
    }

    @Test
    void testSingleInDouble() {
        assertEquals(List.of("a'\n'bc\\d"), read(List.of("\"a'\\n'\"b\"c\"'\\d'")));
    }

    @Test
    void testDoubleInSingle() {
        assertEquals(List.of("a\"\\n\"bcd"), read(List.of("'a\"\\n\"'b'c'\"\\d\"")));
    }

    private List<String> read(List<String> lines) {
        Path path = Mockito.mock(Path.class);
        Either<ParseRequestExpand.NumberedLineResult, List<String>> either = new ParseRequestExpand(path, List.of())
                .readAtLines(lines);
        assertTrue(either.isRight());
        return either.fold(l -> {
            throw new RuntimeException("expecting Right");
        }, Function.identity());
    }

    private ParseRequestExpand.NumberedLineResult expectError(List<String> lines) {
        Path path = Mockito.mock(Path.class);
        Either<ParseRequestExpand.NumberedLineResult, List<String>> either = new ParseRequestExpand(path, List.of())
                .readAtLines(lines);
        assertTrue(either.isLeft());
        return either.fold(Function.identity(), l -> {
            throw new RuntimeException("expecting Left");
        });
    }
}
