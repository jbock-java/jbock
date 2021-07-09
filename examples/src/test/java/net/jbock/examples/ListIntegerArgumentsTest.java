package net.jbock.examples;

import io.jbock.util.Either;
import net.jbock.examples.fixture.ParserTestFixture;
import net.jbock.util.HelpRequested;
import net.jbock.util.NotSuccess;
import net.jbock.util.ParseRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ListIntegerArgumentsTest {

    private final ListIntegerArgumentsParser parser = new ListIntegerArgumentsParser();

    private final ParserTestFixture<ListIntegerArguments> f =
            ParserTestFixture.create(parser::parse);

    @Test
    void testPresent() {
        f.assertThat("-a", "1")
                .has(ListIntegerArguments::a, List.of(1));
    }

    @Test
    void testAbsent() {
        ParseRequest request = ParseRequest.simple(List.of()).withHelpRequested(true).build();
        Either<NotSuccess, ListIntegerArguments> result = parser.parse(request);
        assertTrue(result.isLeft());
        result.getLeft().ifPresent(l -> assertTrue(l instanceof HelpRequested));
    }
}