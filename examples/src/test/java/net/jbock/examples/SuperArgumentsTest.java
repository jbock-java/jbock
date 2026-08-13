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
        String[] args = {"-q", "word", "--", "a"};
        SuperArguments result = f.parse(args);
        assertEquals("word", result.command());
        assertEquals(List.of("--", "a"), result.rest());
    }

    @Test
    void testHelp() {
        f.assertPrintsHelp(
                SuperArgumentsParser.createModel(),
                "USAGE",
                "  super-arguments [OPTIONS] COMMAND REST...",
                "",
                "PARAMETERS",
                "  COMMAND ",
                "  REST    ",
                "",
                "OPTIONS",
                "  -q, --quiet ",
                "");
    }
}
