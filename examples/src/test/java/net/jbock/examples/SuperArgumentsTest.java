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
        SuperArguments result = success.getCommand();
        assertEquals("foo", result.command());
        assertTrue(result.quiet());
        assertArrayEquals(new String[]{"-a", "1"}, success.getRest());
    }

    @Test
    void testDoubleEscape() {
        String[] args = {"-q", "--", "--", "a"};
        SuperResult<SuperArguments> result = f.parse(args);
        assertArrayEquals(new String[]{"--", "a"}, result.getRest());
    }

    @Test
    void testEscapeSequenceNotRecognized() {
        String[] args = {"-q", "--"};
        SuperResult<SuperArguments> result = f.parse(args);
        assertEquals("--", result.getCommand().command());
        assertEquals(0, result.getRest().length);
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
