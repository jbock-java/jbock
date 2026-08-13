package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.List;

class RequiredArgumentsTest {

    private final ParserTestFixture<RequiredArguments> f =
            ParserTestFixture.create(RequiredArgumentsParser::parse);

    @Test
    void success() {
        f.assertThat("--dir", "A")
                .has(RequiredArguments::dir, "A")
                .has(RequiredArguments::otherTokens, List.of());
    }

    @Test
    void errorRepeatedArgument() {
        f.assertThat("--dir", "A", "--dir", "B").fails("Option '--dir' is a repetition");
        f.assertThat("--dir=A", "--dir", "B").fails("Option '--dir' is a repetition");
        f.assertThat("--dir=A", "--dir=B").fails("Option '--dir=B' is a repetition");
        f.assertThat("--dir", "A", "--dir=B").fails("Option '--dir=B' is a repetition");
    }

    @Test
    void errorDetachedAttached() {
        f.assertThat("--dir", "A", "--dir=B").fails("Option '--dir=B' is a repetition");
    }

    @Test
    void testPrint() {
        f.assertPrintsHelp(
                RequiredArgumentsParser.createModel(),
                "USAGE",
                "  required-arguments --dir DIR OTHER_TOKENS...",
                "",
                "PARAMETERS",
                "  OTHER_TOKENS ",
                "",
                "OPTIONS",
                "  --dir DIR ",
                "");
    }
}
