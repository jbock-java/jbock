package net.jbock.examples;

import io.jbock.util.Either;
import net.jbock.examples.fixture.ParserTestFixture;
import net.jbock.util.HelpRequested;
import net.jbock.util.NotSuccess;
import net.jbock.util.ParseRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void errorDirMissing() {
        ParseRequest request = ParseRequest.simple(List.of()).withHelpRequested(true).build();
        Either<NotSuccess, RequiredArguments> result = new RequiredArgumentsParser().parse(request);
        Assertions.assertTrue(result.isLeft());
        result.getLeft().ifPresent(l -> assertTrue(l instanceof HelpRequested));
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
