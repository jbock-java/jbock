package net.jbock.examples;

import io.jbock.util.Either;
import net.jbock.examples.fixture.ParserTestFixture;
import net.jbock.util.HelpRequested;
import net.jbock.util.NotSuccess;
import net.jbock.util.ParseRequest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertTrue;

class OptionalIntArgumentsTest {

    private final OptionalIntArgumentsParser parser = new OptionalIntArgumentsParser();

    private final ParserTestFixture<OptionalIntArguments> f =
            ParserTestFixture.create(parser::parse);

    @Test
    void testPresent() {
        f.assertThat("-a", "1").has(OptionalIntArguments::a, OptionalInt.of(1));
    }

    @Test
    void testAbsent() {
        ParseRequest request = ParseRequest.simple(List.of()).withHelpRequested(true).build();
        Either<NotSuccess, OptionalIntArguments> result = parser.parse(request);
        assertTrue(result.isLeft());
        result.acceptLeft(l -> assertTrue(l instanceof HelpRequested));
    }
}
