package net.jbock.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParseRequestTest {

    @Test
    void testParseList() {
        ParseRequest parseRequest = ParseRequest.from(List.of("a"));
        assertEquals(parseRequest.expand().fold(l -> null, r -> r), List.of("a"));
    }

    @Test
    void testParseArray() {
        ParseRequest parseRequest = ParseRequest.from(new String[]{"a"});
        assertEquals(parseRequest.expand().fold(l -> null, r -> r), List.of("a"));
    }
}
