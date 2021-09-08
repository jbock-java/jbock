package net.jbock.parse;

import net.jbock.util.ExToken;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    void testModeFlagRepetition() {
        Map<String, String> optionNames = Map.of("-a", "A");
        Map<String, OptionState> optionStates = Map.of("A", new OptionStateModeFlag());
        RestlessParser<String> parser = RestlessParser.create(optionNames, optionStates, 0);
        assertThrows(ExToken.class, () -> parser.parse(List.of("-a", "-a")));
    }

    @Test
    void testOptionRepetition() {
        Map<String, String> optionNames = Map.of("-a", "A");
        Map<String, OptionState> optionStates = Map.of("A", new OptionStateNonRepeatable());
        RestlessParser<String> parser = RestlessParser.create(optionNames, optionStates, 0);
        assertThrows(ExToken.class, () -> parser.parse(List.of("-a1", "-a1")));
    }

    @Test
    void testMissingOptionArgument() {
        Map<String, String> optionNames = Map.of("-a", "A");
        Map<String, OptionState> optionStates = Map.of("A", new OptionStateNonRepeatable());
        RestlessParser<String> parser = RestlessParser.create(optionNames, optionStates, 0);
        assertThrows(ExToken.class, () -> parser.parse(List.of("-a")));
    }
}
