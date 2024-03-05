package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

class PsArgumentsTest {

    private final ParserTestFixture<PsArguments> f =
            ParserTestFixture.create(PsArgumentsParser::parse);

    @Test
    void testPrint() {
        f.assertPrintsHelp(
                PsArgumentsParser.createModel(),
                "\u001B[1mUSAGE\u001B[m",
                "  ps-arguments [OPTIONS]",
                "",
                "\u001B[1mOPTIONS\u001B[m",
                "  -a, --all         ",
                "  -w, --width WIDTH  This is the description.",
                "");
    }
}
