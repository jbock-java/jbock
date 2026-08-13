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
                "USAGE",
                "  ps-arguments [OPTIONS]",
                "",
                "OPTIONS",
                "  -a, --all         ",
                "  -w, --width WIDTH  This is the description.",
                "");
    }
}
