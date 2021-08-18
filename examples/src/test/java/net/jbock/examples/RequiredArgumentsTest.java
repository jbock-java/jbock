package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.List;

class RequiredArgumentsTest {

    private final RequiredArgumentsParser parser = new RequiredArgumentsParser();

    private final ParserTestFixture<RequiredArguments> f =
            ParserTestFixture.create(parser::parse);

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
                parser.createModel(),
                "\u001B[1mUSAGE\u001B[m",
                "  required-arguments --dir DIR OTHER_TOKENS...",
                "",
                "\u001B[1mPARAMETERS\u001B[m",
                "  OTHER_TOKENS ",
                "",
                "\u001B[1mOPTIONS\u001B[m",
                "  --dir DIR ",
                "");
    }
}
