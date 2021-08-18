package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import net.jbock.util.SuperResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SuperArgumentsTest {

    private final SuperArgumentsParser parser = new SuperArgumentsParser();

    private final ParserTestFixture<SuperResult<SuperArguments>> f =
            ParserTestFixture.create(parser::parse);

    @Test
    void testRest() {
        SuperResult<SuperArguments> success = f.parse("-q", "foo", "-a", "1");
        SuperArguments result = success.result();
        assertEquals("foo", result.command());
        assertTrue(result.quiet());
        assertArrayEquals(new String[]{"-a", "1"}, success.rest());
    }

    @Test
    void testDoubleEscape() {
        String[] args = {"-q", "--", "--", "a"};
        SuperResult<SuperArguments> result = f.parse(args);
        assertArrayEquals(new String[]{"--", "a"}, result.rest());
    }

    @Test
    void testEscapeSequenceNotRecognized() {
        String[] args = {"-q", "--"};
        SuperResult<SuperArguments> result = f.parse(args);
        assertEquals("--", result.result().command());
        assertEquals(0, result.rest().length);
    }

    @Test
    void testHelp() {
        f.assertPrintsHelp(
                parser.createModel(),
                "\u001B[1mUSAGE\u001B[m",
                "  super-arguments [OPTIONS] COMMAND",
                "",
                "\u001B[1mPARAMETERS\u001B[m",
                "  COMMAND ",
                "",
                "\u001B[1mOPTIONS\u001B[m",
                "  -q, --quiet ",
                "");
    }
}
