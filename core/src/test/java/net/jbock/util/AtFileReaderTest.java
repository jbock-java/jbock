package net.jbock.util;

import io.jbock.util.Either;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AtFileReaderTest {

    private final AtFileReader reader = new AtFileReader();

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
    void testEscapeAtEndOfFile() {
        AtFileReader.NumberedLineResult error = expectError(List.of("'a'\\"));
        assertTrue(error.lineResult().isError());
        assertEquals(1, error.number());
        assertEquals("backslash at end of file", error.lineResult().message());
    }

    @Test
    void testUnmatchedSingleQuote() {
        AtFileReader.NumberedLineResult error = expectError(List.of("'"));
        assertTrue(error.lineResult().isError());
        assertEquals(1, error.number());
        assertEquals("unmatched quote", error.lineResult().message());
    }

    @Test
    void testUnmatchedDoubleQuote() {
        AtFileReader.NumberedLineResult error = expectError(List.of("\""));
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
        List<String> tokens = read(List.of("''"));
        assertEquals(List.of(""), tokens);
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
        Either<AtFileReader.NumberedLineResult, List<String>> either = reader.readAtLines(lines);
        assertTrue(either.isRight());
        return either.fold(l -> {
            throw new RuntimeException("expecting Right");
        }, Function.identity());
    }

    private AtFileReader.NumberedLineResult expectError(List<String> lines) {
        Either<AtFileReader.NumberedLineResult, List<String>> either = reader.readAtLines(lines);
        assertTrue(either.isLeft());
        return either.fold(Function.identity(), l -> {
            throw new RuntimeException("expecting Left");
        });
    }
}
