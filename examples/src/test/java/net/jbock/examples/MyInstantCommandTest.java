package net.jbock.examples;

import net.jbock.util.Either;
import net.jbock.util.ParsingFailed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MyInstantCommandTest {

    @Test
    void testParseInstant() {
        Either<ParsingFailed, MyInstantCommand> result = MyInstantCommandParser.parse(List.of("true"));
        assertTrue(result.isLeft());
        assertEquals("Text 'true' could not be parsed at index 0",
                result.getLeft().orElseThrow().message());
    }
}
