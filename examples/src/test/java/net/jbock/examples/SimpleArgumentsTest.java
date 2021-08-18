package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class SimpleArgumentsTest {

    private final SimpleArgumentsParser parser = new SimpleArgumentsParser();

    private final ParserTestFixture<SimpleArguments> f =
            ParserTestFixture.create(parser::parse);

    @Test
    void invalidOptions() {
        f.assertThat("xf", "1").fails("Excess param: xf");
        f.assertThat("-xf", "1").fails("Invalid option: -xf");
        f.assertThat("---").fails("Invalid option: ---");
    }

    @Test
    void success() {
        f.assertThat("--file", "1")
                .has(SimpleArguments::extract, false)
                .has(SimpleArguments::file, Optional.of("1"));
    }

    @Test
    void errorHelpNotFirstArgument() {
        f.assertThat("--file", "1", "--help").fails("Invalid option: --help");
    }

    @Test
    void testPrint() {
        f.assertPrintsHelp(
                parser.createModel(),
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                "",
                "\u001B[1mUSAGE\u001B[m",
                "  simple-arguments [OPTIONS]",
                "",
                "\u001B[1mOPTIONS\u001B[m",
                "  -x, --x      aa",
                "               AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                "               AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                "               aa aa",
                "  --file FILE ",
                "");
    }
}
