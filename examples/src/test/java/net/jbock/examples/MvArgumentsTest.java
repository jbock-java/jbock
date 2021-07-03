package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

class MvArgumentsTest {

    private final MvArgumentsParser parser = new MvArgumentsParser();

    private final ParserTestFixture<MvArguments> f =
            ParserTestFixture.create(parser::parse);

    @Test
    void notEnoughArguments() {
        f.assertThat("a").fails("Missing required parameter DEST");
    }

    @Test
    void invalidOption() {
        f.assertThat("-aa", "b").fails("Invalid option: -aa");
    }

    @Test
    void excessParam() {
        f.assertThat("a", "b", "c").fails("Excess param: c");
    }

    @Test
    void invalidOptionEscapeSequenceThird() {
        f.assertThat("a", "b", "--", "c").fails("Excess param: c");
    }

    @Test
    void validInvocation() {
        f.assertThat("a", "b")
                .has(MvArguments::source, "a")
                .has(MvArguments::dest, "b");
    }

    @Test
    void valid() {
        f.assertThat("a", "b")
                .has(MvArguments::source, "a")
                .has(MvArguments::dest, "b");
    }

    @Test
    void testPrint() {
        f.assertPrintsHelp(
                "\u001B[1mUSAGE\u001B[m",
                "  mv-arguments SOURCE DEST",
                "",
                "\u001B[1mPARAMETERS\u001B[m",
                "  SOURCE ",
                "  DEST   ",
                "");
    }
}
