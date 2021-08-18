package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.List;

class ListIntegerArgumentsTest {

    private final ListIntegerArgumentsParser parser = new ListIntegerArgumentsParser();

    private final ParserTestFixture<ListIntegerArguments> f =
            ParserTestFixture.create(parser::parse);

    @Test
    void testPresent() {
        f.assertThat("-a", "1")
                .has(ListIntegerArguments::a, List.of(1));
    }
}