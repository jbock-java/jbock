package net.jbock.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParseRequestTest {

    @Test
    void testParseList() {
        ParseRequest parseRequest = ParseRequest.from(List.of("a"));
        assertTrue(parseRequest.expand().isRight());
        assertEquals(parseRequest.expand().getRight().orElseThrow(), List.of("a"));
    }

    @Test
    void testParseArray() {
        ParseRequest parseRequest = ParseRequest.from(new String[]{"a"});
        assertTrue(parseRequest.expand().isRight());
        assertEquals(parseRequest.expand().getRight().orElseThrow(), List.of("a"));
    }
}
