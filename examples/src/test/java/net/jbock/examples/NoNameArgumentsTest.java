package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

class NoNameArgumentsTest {

    private final NoNameArgumentsParser parser = new NoNameArgumentsParser();

    private final ParserTestFixture<NoNameArguments> f =
            ParserTestFixture.create(parser::parse);

    @Test
    void testDifferentOrder() {
        f.assertThat("--message=m", "--file=f", "--file=o", "--file=o", "--cmos", "-n1")
                .has(NoNameArguments::message, Optional.of("m"))
                .has(NoNameArguments::file, List.of("f", "o", "o"))
                .has(NoNameArguments::verbosity, Optional.empty())
                .has(NoNameArguments::number, 1)
                .has(NoNameArguments::cmos, true);
        f.assertThat("-n1", "--cmos", "--message=m", "--file=f", "--file=o", "--file=o")
                .has(NoNameArguments::message, Optional.of("m"))
                .has(NoNameArguments::file, List.of("f", "o", "o"))
                .has(NoNameArguments::verbosity, Optional.empty())
                .has(NoNameArguments::number, 1)
                .has(NoNameArguments::cmos, true);
        f.assertThat("--file", "f", "--message=m", "--file", "o", "--cmos", "-n1", "--file", "o")
                .has(NoNameArguments::message, Optional.of("m"))
                .has(NoNameArguments::file, List.of("f", "o", "o"))
                .has(NoNameArguments::verbosity, Optional.empty())
                .has(NoNameArguments::number, 1)
                .has(NoNameArguments::cmos, true);
    }

    @Test
    void testFlag() {
        f.assertThat("--cmos", "-n1")
                .has(NoNameArguments::message, Optional.empty())
                .has(NoNameArguments::file, List.of())
                .has(NoNameArguments::verbosity, Optional.empty())
                .has(NoNameArguments::number, 1)
                .has(NoNameArguments::cmos, true);
    }

    @Test
    void testOptionalInt() {
        f.assertThat("-v", "1", "-n1")
                .has(NoNameArguments::message, Optional.empty())
                .has(NoNameArguments::file, List.of())
                .has(NoNameArguments::verbosity, Optional.of(1))
                .has(NoNameArguments::number, 1)
                .has(NoNameArguments::cmos, false);
        f.assertThat("-n1")
                .has(NoNameArguments::message, Optional.empty())
                .has(NoNameArguments::file, List.of())
                .has(NoNameArguments::verbosity, Optional.empty())
                .has(NoNameArguments::number, 1)
                .has(NoNameArguments::cmos, false);
    }

    @Test
    void errorMissingInt() {
        f.assertThat("--cmos").fails("Missing required option NUMBER (-n, --number)");
    }

    @Test
    void errorUnknownToken() {
        f.assertThat("blabla").fails("Excess param: blabla");
    }

    @Test
    void testPrint() {
        f.assertPrintsHelp(
                "\u001B[1mUSAGE\u001B[m",
                "  no-name-arguments [OPTIONS] -n NUMBER",
                "",
                "\u001B[1mOPTIONS\u001B[m",
                "  --message MESSAGE         ",
                "  --file FILE               ",
                "  -v, --verbosity VERBOSITY ",
                "  -n, --number NUMBER       ",
                "  --cmos                    ",
                "");
    }

}
