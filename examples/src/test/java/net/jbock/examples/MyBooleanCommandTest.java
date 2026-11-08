package net.jbock.examples;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MyBooleanCommandTest {

    @Test
    void testParseBoolean() {
        MyBooleanCommand result = MyBooleanCommandParser.parse(List.of("true")).getRight().orElseThrow();
        assertTrue(result.isBoolean());
        result = MyBooleanCommandParser.parse(List.of("nein")).getRight().orElseThrow();
        assertFalse(result.isBoolean());
    }
}
