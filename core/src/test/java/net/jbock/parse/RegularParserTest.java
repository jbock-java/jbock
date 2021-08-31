package net.jbock.parse;

import net.jbock.util.ExToken;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegularParserTest {

    @Test
    void testZeroParamsFail() {
        RegularParser<String> parser = RegularParser.create(Map.of(), Map.of(), 0);
        assertThrows(ExToken.class, () -> parser.parse(List.of("1")));
    }

    @Test
    void testZeroParamsSuccess() throws ExToken {
        RegularParser<String> parser = RegularParser.create(Map.of(), Map.of(), 0);
        parser.parse(List.of());
        assertTrue(parser.rest().toList().isEmpty());
        assertTrue(parser.option("a").findAny().isEmpty());
        assertTrue(parser.param(0).isEmpty());
    }

    @Test
    void testOneParamsFail() {
        RegularParser<String> parser = RegularParser.create(Map.of(), Map.of(), 1);
        assertThrows(ExToken.class, () -> parser.parse(List.of("1", "2")));
    }

    @Test
    void testOneParamSuccess() throws ExToken {
        RegularParser<String> parser = RegularParser.create(Map.of(), Map.of(), 1);
        parser.parse(List.of("1"));
        assertTrue(parser.rest().toList().isEmpty());
        assertTrue(parser.option("a").findAny().isEmpty());
        assertEquals(Optional.of("1"), parser.param(0));
    }
}