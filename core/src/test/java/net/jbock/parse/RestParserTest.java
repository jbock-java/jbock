package net.jbock.parse;

import net.jbock.util.ExToken;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RestParserTest {

    @Test
    void testZeroParamsExcess() throws ExToken {
        RestParser<String> parser = RestParser.create(Map.of(), Map.of(), 0);
        parser.parse(List.of("1"));
        assertEquals(List.of("1"), parser.rest().toList());
    }

    @Test
    void testOneParamExcess() throws ExToken {
        RestParser<String> parser = RestParser.create(Map.of(), Map.of(), 1);
        parser.parse(List.of("1", "2"));
        assertEquals(Optional.of("1"), parser.param(0));
        assertEquals(List.of("2"), parser.rest().toList());
    }
}
