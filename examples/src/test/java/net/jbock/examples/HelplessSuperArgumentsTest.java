package net.jbock.examples;

import io.jbock.util.Either;
import net.jbock.examples.fixture.ParserTestFixture;
import net.jbock.util.ParsingFailed;
import org.junit.jupiter.api.Test;

import java.util.List;

class HelplessSuperArgumentsTest {

    private final ParserTestFixture<HelplessSuperArguments> f =
            ParserTestFixture.create(HelplessSuperArgumentsParser::parse);

    @Test
    void testHelp() {
        Either<ParsingFailed, HelplessSuperArguments> result =
                HelplessSuperArgumentsParser.parse(List.of("--help"));
        f.assertThat(result).fails("Invalid option: --help");
    }
}
