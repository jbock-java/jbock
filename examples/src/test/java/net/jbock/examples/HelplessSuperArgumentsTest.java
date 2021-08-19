package net.jbock.examples;

import io.jbock.util.Either;
import net.jbock.examples.fixture.ParserTestFixture;
import net.jbock.util.ParsingFailed;
import net.jbock.util.SuperResult;
import org.junit.jupiter.api.Test;

import java.util.List;

class HelplessSuperArgumentsTest {

    private final HelplessSuperArgumentsParser parser = new HelplessSuperArgumentsParser();

    private final ParserTestFixture<SuperResult<HelplessSuperArguments>> f =
            ParserTestFixture.create(parser::parse);

    @Test
    void testHelp() {
        Either<ParsingFailed, SuperResult<HelplessSuperArguments>> result =
                parser.parse(List.of("--help"));
        f.assertThat(result).fails("Invalid option: --help");
    }
}
