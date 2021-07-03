package net.jbock.examples;

import io.jbock.util.Optional;
import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.List;

class GradleArgumentsTest {

    private final GradleArgumentsParser parser = new GradleArgumentsParser();

    private final ParserTestFixture<GradleArguments> f =
            ParserTestFixture.create(parser::parse);

    @Test
    void errorShortLongConflict() {
        f.assertThat("-m", "hello", "--message=goodbye")
                .fails("Option '--message=goodbye' is a repetition");
    }

    @Test
    void errorMissingValue() {
        f.assertThat("-m").fails("Missing argument after option name: -m");
    }

    @Test
    void errorLongShortConflict() {
        f.assertThat("--message=hello", "-m", "goodbye")
                .fails("Option '-m' is a repetition");
    }

    @Test
    void errorLongLongConflict() {
        f.assertThat("--message=hello", "--message=goodbye")
                .fails("Option '--message=goodbye' is a repetition");
    }

    @Test
    void errorInvalidOption() {
        f.assertThat("-c1").fails("Invalid token: -c1");
        f.assertThat("-c-v").fails("Invalid token: -c-v");
        f.assertThat("-c-").fails("Invalid token: -c-");
        f.assertThat("-c=v").fails("Invalid token: -c=v");
        f.assertThat("-c=").fails("Invalid token: -c=");
        f.assertThat("-cX=1").fails("Invalid token: -cX=1");
        f.assertThat("-cvv").fails("Option '-v' is a repetition");
        f.assertThat("-cvx").fails("Invalid token: -cvx");
        f.assertThat("-cvm").fails("Missing argument after option name: -m");
        f.assertThat("--column-count").fails("Invalid option: --column-count");
        f.assertThat("--cmos").fails("Invalid option: --cmos");
    }

    @Test
    void testDetachedLong() {
        f.assertThat("--message", "hello")
                .has(GradleArguments::message, Optional.of("hello"))
                .has(GradleArguments::file, List.of())
                .has(GradleArguments::dir, Optional.empty())
                .has(GradleArguments::cmos, false)
                .has(GradleArguments::verbose, false)
                .has(GradleArguments::otherTokens, List.of());
    }

    @Test
    void testInterestingTokens() {
        f.assertThat("--message=hello", "b-a-b-a", "--", "->", "<=>", "", " ")
                .has(GradleArguments::message, Optional.of("hello"))
                .has(GradleArguments::file, List.of())
                .has(GradleArguments::dir, Optional.empty())
                .has(GradleArguments::cmos, false)
                .has(GradleArguments::verbose, false)
                .has(GradleArguments::mainToken, Optional.of("b-a-b-a"))
                .has(GradleArguments::otherTokens, List.of("->", "<=>", "", " "));
    }

    @Test
    void testPassEmptyString() {
        f.assertThat("-m", "")
                .has(GradleArguments::message, Optional.of(""))
                .has(GradleArguments::file, List.of())
                .has(GradleArguments::dir, Optional.empty())
                .has(GradleArguments::cmos, false)
                .has(GradleArguments::verbose, false)
                .has(GradleArguments::otherTokens, List.of());
        f.assertThat("--message=")
                .has(GradleArguments::message, Optional.of(""))
                .has(GradleArguments::file, List.of())
                .has(GradleArguments::dir, Optional.empty())
                .has(GradleArguments::cmos, false)
                .has(GradleArguments::verbose, false)
                .has(GradleArguments::otherTokens, List.of());
        f.assertThat("--message", "")
                .has(GradleArguments::message, Optional.of(""))
                .has(GradleArguments::file, List.of())
                .has(GradleArguments::dir, Optional.empty())
                .has(GradleArguments::cmos, false)
                .has(GradleArguments::verbose, false)
                .has(GradleArguments::otherTokens, List.of());
    }

    @Test
    void testAllForms() {
        f.assertThat("-mhello")
                .has(GradleArguments::message, Optional.of("hello"))
                .has(GradleArguments::file, List.of())
                .has(GradleArguments::dir, Optional.empty())
                .has(GradleArguments::cmos, false)
                .has(GradleArguments::verbose, false)
                .has(GradleArguments::otherTokens, List.of());
        f.assertThat("-m", "hello")
                .has(GradleArguments::message, Optional.of("hello"))
                .has(GradleArguments::file, List.of())
                .has(GradleArguments::dir, Optional.empty())
                .has(GradleArguments::cmos, false)
                .has(GradleArguments::verbose, false)
                .has(GradleArguments::otherTokens, List.of());
        f.assertThat("--message=hello")
                .has(GradleArguments::message, Optional.of("hello"))
                .has(GradleArguments::file, List.of())
                .has(GradleArguments::dir, Optional.empty())
                .has(GradleArguments::cmos, false)
                .has(GradleArguments::verbose, false)
                .has(GradleArguments::otherTokens, List.of());
        f.assertThat("--message", "hello")
                .has(GradleArguments::message, Optional.of("hello"))
                .has(GradleArguments::file, List.of())
                .has(GradleArguments::dir, Optional.empty())
                .has(GradleArguments::cmos, false)
                .has(GradleArguments::verbose, false)
                .has(GradleArguments::otherTokens, List.of());
    }

    @Test
    void testRepeatableShortAttached() {
        f.assertThat("-fbar.txt")
                .has(GradleArguments::message, Optional.empty())
                .has(GradleArguments::file, List.of("bar.txt"))
                .has(GradleArguments::dir, Optional.empty())
                .has(GradleArguments::cmos, false)
                .has(GradleArguments::verbose, false)
                .has(GradleArguments::otherTokens, List.of());
        f.assertThat("-fbar.txt", "--message=hello")
                .has(GradleArguments::message, Optional.of("hello"))
                .has(GradleArguments::file, List.of("bar.txt"))
                .has(GradleArguments::dir, Optional.empty())
                .has(GradleArguments::cmos, false)
                .has(GradleArguments::verbose, false)
                .has(GradleArguments::otherTokens, List.of());
        f.assertThat("--message=hello", "-fbar.txt")
                .has(GradleArguments::message, Optional.of("hello"))
                .has(GradleArguments::file, List.of("bar.txt"))
                .has(GradleArguments::dir, Optional.empty())
                .has(GradleArguments::cmos, false)
                .has(GradleArguments::verbose, false)
                .has(GradleArguments::otherTokens, List.of());
    }

    @Test
    void testFlag() {
        f.assertThat("-c", "hello", "hello")
                .has(GradleArguments::message, Optional.empty())
                .has(GradleArguments::file, List.of())
                .has(GradleArguments::dir, Optional.empty())
                .has(GradleArguments::cmos, true)
                .has(GradleArguments::verbose, false)
                .has(GradleArguments::mainToken, Optional.of("hello"))
                .has(GradleArguments::otherTokens, List.of("hello"));
    }

    @Test
    void testPositionalOnly() {
        f.assertThat("hello", "goodbye")
                .has(GradleArguments::message, Optional.empty())
                .has(GradleArguments::file, List.of())
                .has(GradleArguments::dir, Optional.empty())
                .has(GradleArguments::cmos, false)
                .has(GradleArguments::verbose, false)
                .has(GradleArguments::mainToken, Optional.of("hello"))
                .has(GradleArguments::otherTokens, List.of("goodbye"));
    }

    @Test
    void twoFlags() {
        f.assertThat("-c", "-v")
                .has(GradleArguments::message, Optional.empty())
                .has(GradleArguments::file, List.of())
                .has(GradleArguments::dir, Optional.empty())
                .has(GradleArguments::cmos, true)
                .has(GradleArguments::verbose, true)
                .has(GradleArguments::otherTokens, List.of());
    }

    @Test
    void testClustering() {
        f.assertThat("-cv")
                .has(GradleArguments::message, Optional.empty())
                .has(GradleArguments::file, List.of())
                .has(GradleArguments::dir, Optional.empty())
                .has(GradleArguments::cmos, true)
                .has(GradleArguments::verbose, true)
                .has(GradleArguments::otherTokens, List.of());
        f.assertThat("-cvm", "hello")
                .has(GradleArguments::message, Optional.of("hello"))
                .has(GradleArguments::file, List.of())
                .has(GradleArguments::dir, Optional.empty())
                .has(GradleArguments::cmos, true)
                .has(GradleArguments::verbose, true)
                .has(GradleArguments::otherTokens, List.of());
        f.assertThat("-cvmhello")
                .has(GradleArguments::message, Optional.of("hello"))
                .has(GradleArguments::file, List.of())
                .has(GradleArguments::dir, Optional.empty())
                .has(GradleArguments::cmos, true)
                .has(GradleArguments::verbose, true)
                .has(GradleArguments::otherTokens, List.of());
    }

    @Test
    void testPrint() {
        f.assertPrintsHelp(
                "\u001B[1mUSAGE\u001B[m",
                "  gradle-arguments [OPTIONS] [SOME_TOKEN] moreTokens...",
                "",
                "\u001B[1mPARAMETERS\u001B[m",
                "  SOME_TOKEN  some token",
                "  moreTokens  some more tokens",
                "",
                "\u001B[1mOPTIONS\u001B[m",
                "  -m, --message MESSAGE  the message message goes here",
                "  -f, --file INPUT_FILE  the files",
                "  --dir INPUT_DIR        the dir",
                "  -c, --c                cmos flag",
                "  -v, --verbose         ",
                "");
    }
}
