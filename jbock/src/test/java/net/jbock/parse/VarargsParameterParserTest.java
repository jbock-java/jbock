package net.jbock.parse;

import net.jbock.util.ExToken;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VarargsParameterParserTest {

    @Test
    void testZeroParamsExcess() throws ExToken {
        VarargsParameterParser parser = VarargsParameterParser.create(Map.of(), new OptionState[0], 0);
        parser.parse(List.of("1"));
        assertEquals(List.of("1"), parser.rest().toList());
    }

    @Test
    void testOneParamExcess() throws ExToken {
        VarargsParameterParser parser = VarargsParameterParser.create(Map.of(), new OptionState[0], 1);
        parser.parse(List.of("1", "2"));
        assertEquals(Optional.of("1"), parser.param(0));
        assertEquals(List.of("2"), parser.rest().toList());
    }

    @Test
    void testSuspicious() throws ExToken {
        StandardParser parser = StandardParser.create(Map.of(), new OptionState[0], 1);
        assertThrows(ExToken.class, () -> parser.parse(List.of("-a")));
        assertThrows(ExToken.class, () -> parser.parse(List.of("--as")));
        parser.parse(List.of("as"));
        assertEquals(Optional.of("as"), parser.param(0));
    }

    @Test
    void testModeFlagRepetition() {
        Map<String, Integer> optionNames = Map.of("-a", 0);
        OptionState[] optionStates = new OptionState[1];
        optionStates[0] = new OptionStateModeFlag();
        StandardParser parser = StandardParser.create(optionNames, optionStates, 0);
        assertThrows(ExToken.class, () -> parser.parse(List.of("-a", "-a")));
    }

    @Test
    void testOptionNonRepeatableRepetition() {
        Map<String, Integer> optionNames = Map.of("-a", 0);
        OptionState[] optionStates = new OptionState[1];
        optionStates[0] = new OptionStateNonRepeatable();
        StandardParser parser = StandardParser.create(optionNames, optionStates, 0);
        assertThrows(ExToken.class, () -> parser.parse(List.of("-a1", "-a1")));
    }

    @Test
    void testOptionRepeatableRepetition() throws ExToken {
        Map<String, Integer> optionNames = Map.of("-a", 0);
        OptionState[] optionStates = new OptionState[1];
        optionStates[0] = new OptionStateRepeatable();
        StandardParser parser = StandardParser.create(optionNames, optionStates, 0);
        parser.parse(List.of("-a1", "-a2"));
        assertEquals(List.of("1", "2"), parser.option(0).toList());
    }

    @Test
    void testMissingOptionArgument() {
        Map<String, Integer> optionNames = Map.of("-a", 0);
        OptionState[] optionStates = new OptionState[1];
        optionStates[0] = new OptionStateNonRepeatable();
        StandardParser parser = StandardParser.create(optionNames, optionStates, 0);
        assertThrows(ExToken.class, () -> parser.parse(List.of("-a")));
    }
}
