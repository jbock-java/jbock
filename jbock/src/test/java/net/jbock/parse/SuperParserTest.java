package net.jbock.parse;

import net.jbock.util.ExToken;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SuperParserTest {

    @Test
    void testZeroParamsExcess() throws ExToken {
        SuperParser parser = SuperParser.create(Map.of(), new OptionState[0], 0);
        parser.parse(List.of("1"));
        assertEquals(List.of("1"), parser.rest().toList());
    }

    @Test
    void testZeroParamsNotSuspicious() throws ExToken {
        SuperParser parser = SuperParser.create(Map.of(), new OptionState[0], 0);
        parser.parse(List.of("-a"));
        assertEquals(List.of("-a"), parser.rest().toList());
    }

    @Test
    void testZeroParamsSuccessEmpty() throws ExToken {
        SuperParser parser = SuperParser.create(Map.of(), new OptionState[0], 0);
        parser.parse(List.of());
        assertTrue(parser.option(0).findAny().isEmpty());
        assertTrue(parser.param(0).isEmpty());
    }

    @Test
    void testZeroParamsSuccessEscape() throws ExToken {
        SuperParser parser = SuperParser.create(Map.of(), new OptionState[0], 0);
        parser.parse(List.of("--"));
        assertTrue(parser.option(0).findAny().isEmpty());
        assertTrue(parser.param(0).isEmpty());
        assertEquals(List.of("--"), parser.rest().toList());
    }

    @Test
    void testOneParamSuspicious() {
        SuperParser parser = SuperParser.create(Map.of(), new OptionState[0], 1);
        assertThrows(ExToken.class, () -> parser.parse(List.of("-a")));
    }

    @Test
    void testOneParamExcess() throws ExToken {
        SuperParser parser = SuperParser.create(Map.of(), new OptionState[0], 1);
        parser.parse(List.of("1", "2"));
        assertEquals(Optional.of("1"), parser.param(0));
        assertEquals(List.of("2"), parser.rest().toList());
    }

    @Test
    void testOneParamSuccess() throws ExToken {
        SuperParser parser = SuperParser.create(Map.of(), new OptionState[0], 1);
        parser.parse(List.of("1"));
        assertTrue(parser.option(0).findAny().isEmpty());
        assertEquals(Optional.of("1"), parser.param(0));
    }

    @Test
    void testOneUnixOptionAttached() throws ExToken {
        Map<String, Integer> optionNames = Map.of("-a", 0);
        OptionState[] optionStates = new OptionState[1];
        optionStates[0] = new OptionStateNonRepeatable();
        SuperParser parser = SuperParser.create(optionNames, optionStates, 1);
        parser.parse(List.of("-a1"));
        assertEquals(List.of("1"), parser.option(0).toList());
    }

    @Test
    void testOneUnixOptionDetached() throws ExToken {
        Map<String, Integer> optionNames = Map.of("-a", 0);
        OptionState[] optionStates = new OptionState[1];
        optionStates[0] = new OptionStateNonRepeatable();
        SuperParser parser = SuperParser.create(optionNames, optionStates, 1);
        parser.parse(List.of("-a", "1"));
        assertEquals(List.of("1"), parser.option(0).toList());
    }

    @Test
    void testOneUnixOptionClusterFail() {
        Map<String, Integer> optionNames = Map.of("-a", 0);
        OptionState[] optionStates = new OptionState[1];
        optionStates[0] = new OptionStateModeFlag();
        SuperParser parser = SuperParser.create(optionNames, optionStates, 1);
        assertThrows(ExToken.class, () -> parser.parse(List.of("-ab")));
    }

    @Test
    void testOneUnixOptionClusterBadOptionState() throws ExToken {
        Map<String, Integer> optionNames = Map.of("-a", 0);
        OptionState optionState = mock(OptionState.class);
        when(optionState.read(any(), any()))
                .thenReturn("readOptionName_invalid_input");
        OptionState[] optionStates = new OptionState[1];
        optionStates[0] = optionState;
        SuperParser parser = SuperParser.create(optionNames, optionStates, 1);
        assertThrows(ExToken.class, () -> parser.parse(List.of("-a-")));
    }

    @Test
    void testOneGnuOptionAttached() throws ExToken {
        Map<String, Integer> optionNames = Map.of("--alpha", 0);
        OptionState[] optionStates = new OptionState[1];
        optionStates[0] = new OptionStateNonRepeatable();
        SuperParser parser = SuperParser.create(optionNames, optionStates, 1);
        parser.parse(List.of("--alpha=1"));
        assertEquals(List.of("1"), parser.option(0).toList());
    }

    @Test
    void testOneGnuOptionDetached() throws ExToken {
        Map<String, Integer> optionNames = Map.of("--alpha", 0);
        OptionState[] optionStates = new OptionState[1];
        optionStates[0] = new OptionStateNonRepeatable();
        SuperParser parser = SuperParser.create(optionNames, optionStates, 1);
        parser.parse(List.of("--alpha", "1"));
        assertEquals(List.of("1"), parser.option(0).toList());
    }

    @Test
    void testTwoUnixOptionsAttached() throws ExToken {
        Map<String, Integer> optionNames = Map.of("-a", 0, "-b", 1);
        OptionState[] optionStates = new OptionState[2];
        optionStates[0] = new OptionStateModeFlag();
        optionStates[1] = new OptionStateNonRepeatable();
        SuperParser parser = SuperParser.create(optionNames, optionStates, 1);
        parser.parse(List.of("-a", "-b1"));
        assertTrue(parser.option(0).findAny().isPresent());
        assertEquals(List.of("1"), parser.option(1).toList());
    }

    @Test
    void testTwoUnixOptionsDetached() throws ExToken {
        Map<String, Integer> optionNames = Map.of("-a", 0, "-b", 1);
        OptionState[] optionStates = new OptionState[2];
        optionStates[0] = new OptionStateModeFlag();
        optionStates[1] = new OptionStateNonRepeatable();
        SuperParser parser = SuperParser.create(optionNames, optionStates, 1);
        parser.parse(List.of("-a", "-b", "1"));
        assertTrue(parser.option(0).findAny().isPresent());
        assertEquals(List.of("1"), parser.option(1).toList());
    }

    @Test
    void testTwoUnixOptionsClusteringAttached() throws ExToken {
        Map<String, Integer> optionNames = Map.of("-a", 0, "-b", 1);
        OptionState[] optionStates = new OptionState[2];
        optionStates[0] = new OptionStateModeFlag();
        optionStates[1] = new OptionStateNonRepeatable();
        SuperParser parser = SuperParser.create(optionNames, optionStates, 1);
        parser.parse(List.of("-ab1"));
        assertTrue(parser.option(0).findAny().isPresent());
        assertEquals(List.of("1"), parser.option(1).toList());
    }

    @Test
    void testTwoUnixOptionsClusteringDetached() throws ExToken {
        Map<String, Integer> optionNames = Map.of("-a", 0, "-b", 1);
        OptionState[] optionStates = new OptionState[2];
        optionStates[0] = new OptionStateModeFlag();
        optionStates[1] = new OptionStateNonRepeatable();
        SuperParser parser = SuperParser.create(optionNames, optionStates, 1);
        parser.parse(List.of("-ab", "1"));
        assertTrue(parser.option(0).findAny().isPresent());
        assertEquals(List.of("1"), parser.option(1).toList());
    }
}
