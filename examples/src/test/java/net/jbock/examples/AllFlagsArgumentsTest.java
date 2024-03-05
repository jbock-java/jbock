package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

class AllFlagsArgumentsTest {

    private final ParserTestFixture<AllFlagsArguments> f =
            ParserTestFixture.create(AllFlagsArgumentsParser::parse);

    @Test
    void tests() {
        f.assertThat().has(AllFlagsArguments::smallFlag, false);
        f.assertThat("--smallFlag").has(AllFlagsArguments::smallFlag, true);
        f.assertThat("-f").has(AllFlagsArguments::smallFlag, true);
        f.assertThat("-s").has(AllFlagsArguments::smallFlag, true);
    }
}