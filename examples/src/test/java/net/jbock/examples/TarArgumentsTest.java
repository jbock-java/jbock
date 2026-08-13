package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

class TarArgumentsTest {

    private final ParserTestFixture<TarArguments> f =
            ParserTestFixture.create(TarArgumentsParser::parse);

    @Test
    void testExtract() {
        f.assertThat("-x", "-f", "foo.tar")
                .has(TarArguments::extract, true)
                .has(TarArguments::create, false)
                .has(TarArguments::verbose, false)
                .has(TarArguments::compress, false)
                .has(TarArguments::file, "foo.tar");
        f.assertThat("-v", "-x", "-f", "foo.tar")
                .has(TarArguments::extract, true)
                .has(TarArguments::create, false)
                .has(TarArguments::verbose, true)
                .has(TarArguments::compress, false)
                .has(TarArguments::file, "foo.tar");
    }

    @Test
    void unfinishedUnixGroup() {
        f.assertThat("-xf")
                .fails("Missing argument after option name: -f");
    }

    @Test
    void testPrint() {
        String expectation = """
                USAGE
                  tar-arguments [OPTIONS] -f FILE
                
                OPTIONS
                  -x, --x        \s
                  -c, --c        \s
                  -v, --v        \s
                  -z, --z        \s
                  -f, --file FILE\s
                """;
        f.assertPrintsHelpString(TarArgumentsParser.createModel(), expectation);
    }
}
