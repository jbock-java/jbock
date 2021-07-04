package net.jbock.examples;

import io.jbock.util.Either;
import net.jbock.examples.fixture.ParserTestFixture;
import net.jbock.util.NotSuccess;
import net.jbock.util.ParseRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

class HelplessArgumentsTest {

    private final HelplessArgumentsParser parser = new HelplessArgumentsParser();

    private final ParserTestFixture<HelplessArguments> f =
            ParserTestFixture.create(parser::parse);

    @Test
    void testHelpIsAcceptedAsNormalOption() {
        Either<NotSuccess, HelplessArguments> result =
                parser.parse(ParseRequest.simple(List.of("--help", "x"))
                        .withHelpRequested(false)
                        .build());
        f.assertThat(result)
                .has(HelplessArguments::required, "x")
                .has(HelplessArguments::help, true);
    }

    @Test
    void errorNoArguments() {
        Either<NotSuccess, HelplessArguments> result =
                parser.parse(ParseRequest.simple(List.of(/* empty */))
                        .withHelpRequested(false)
                        .build());
        f.assertThat(result)
                .fails("Missing required parameter REQUIRED");
    }

    @Test
    void errorHelpDisabled() {
        Either<NotSuccess, HelplessArguments> result =
                parser.parse(ParseRequest.simple(List.of("--help"))
                        .withHelpRequested(false)
                        .build());
        f.assertThat(result)
                .fails("Missing required parameter REQUIRED");
    }
}
