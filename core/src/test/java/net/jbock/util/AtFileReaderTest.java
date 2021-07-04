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
    void testNewline() {
        List<String> tokens = read(List.of("\\n"));
        assertEquals(List.of("\n"), tokens);
    }

    @Test
    void testSingleQuotes() {
        List<String> tokens = read(List.of("'\\n'"));
        assertEquals(List.of("\\n"), tokens);
    }

    @Test
    void testSingleQuotesEmpty() {
        List<String> tokens = read(List.of("''"));
        assertEquals(List.of(""), tokens);
    }

    private List<String> read(List<String> lines) {
        Either<AtFileReader.NumberedLineResult, List<String>> either = reader.readAtLines(lines);
        assertTrue(either.isRight());
        return either.fold(l -> {
            throw new RuntimeException();
        }, Function.identity());
    }
}