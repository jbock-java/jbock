package net.jbock.examples;

import io.jbock.util.Either;
import net.jbock.examples.fixture.ParserTestFixture;
import net.jbock.util.ParsingFailed;
import org.junit.jupiter.api.Test;

import java.util.List;

class HelplessArgumentsTest {

    private final ParserTestFixture<HelplessArguments> f =
            ParserTestFixture.create(HelplessArgumentsParser::parse);

    @Test
    void testHelpIsAcceptedAsNormalOption() {
        Either<ParsingFailed, HelplessArguments> result =
                HelplessArgumentsParser.parse(List.of("--help", "x"));
        f.assertThat(result)
                .has(HelplessArguments::required, "x")
                .has(HelplessArguments::help, true);
    }

    @Test
    void errorNoArguments() {
        Either<ParsingFailed, HelplessArguments> result =
                HelplessArgumentsParser.parse(List.of(/* empty */));
        f.assertThat(result)
                .fails("Missing required parameter REQUIRED");
    }

    @Test
    void errorHelpDisabled() {
        Either<ParsingFailed, HelplessArguments> result =
                HelplessArgumentsParser.parse(List.of("--help"));
        f.assertThat(result)
                .fails("Missing required parameter REQUIRED");
    }
}
