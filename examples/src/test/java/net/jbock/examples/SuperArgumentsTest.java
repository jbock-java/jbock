package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SuperArgumentsTest {

    private final ParserTestFixture<SuperArguments> f =
            ParserTestFixture.create(SuperArgumentsParser::parse);

    @Test
    void testRest() {
        SuperArguments result = f.parse("-q", "foo", "-a", "1");
        assertEquals("foo", result.command());
        assertTrue(result.quiet());
        assertEquals(List.of("-a", "1"), result.rest());
    }

    @Test
    void testDoubleEscape() {
        String[] args = {"-q", "--", "--", "a"};
        SuperArguments result = f.parse(args);
        assertEquals(List.of("--", "a"), result.rest());
    }

    @Test
    void testEscapeSequenceNotRecognized() {
        String[] args = {"-q", "--"};
        SuperArguments result = f.parse(args);
        assertEquals("--", result.command());
        assertTrue(result.rest().isEmpty());
    }

    @Test
    void testHelp() {
        f.assertPrintsHelp(
                SuperArgumentsParser.createModel(),
                "\u001B[1mUSAGE\u001B[m",
                "  super-arguments [OPTIONS] COMMAND REST...",
                "",
                "\u001B[1mPARAMETERS\u001B[m",
                "  COMMAND ",
                "  REST    ",
                "",
                "\u001B[1mOPTIONS\u001B[m",
                "  -q, --quiet ",
                "");
    }
}
