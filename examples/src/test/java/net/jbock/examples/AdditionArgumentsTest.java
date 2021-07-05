package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class AdditionArgumentsTest {

    private final AdditionArgumentsParser parser = new AdditionArgumentsParser();

    private final ParserTestFixture<AdditionArguments> f =
            ParserTestFixture.create(parser::parse);

    @Test
    void optionalAbsent() {
        f.assertThat("1", "2")
                .has(AdditionArguments::a, 1)
                .has(AdditionArguments::b, 2)
                .has(AdditionArguments::c, Optional.empty());
    }

    @Test
    void optionalPresent() {
        f.assertThat("1", "2", "3")
                .has(AdditionArguments::a, 1)
                .has(AdditionArguments::b, 2)
                .has(AdditionArguments::c, Optional.of(3));
    }

    @Test
    void wrongNumber() {
        f.assertThat("--", "-a", "2").fails("while converting parameter A: For input string: \"-a\"");
    }

    @Test
    void dashesIgnored() {
        f.assertThat("--", "1", "-2", "3")
                .has(AdditionArguments::sum, 2);
        f.assertThat("--", "-1", "-2", "-3")
                .has(AdditionArguments::sum, -6);
        f.assertThat("--", "-1", "-2", "3")
                .has(AdditionArguments::sum, 0);
        f.assertThat("--", "-1", "-2")
                .has(AdditionArguments::sum, -3);
    }

    @Test
    void testPrint() {
        f.assertPrintsHelp(
                "\u001B[1mUSAGE\u001B[m",
                "  addition-arguments A B [C]",
                "",
                "\u001B[1mPARAMETERS\u001B[m",
                "  A  First argument",
                "  B  Second argument",
                "  C  Optional third argument",
                "");
    }
}
