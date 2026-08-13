package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class GradleArgumentsFooTest {

    private final ParserTestFixture<GradleArguments.Foo> f =
            ParserTestFixture.create(GradleArguments_FooParser::parse);

    @Test
    void testParserForNestedClass() {
        f.assertThat("--bar=4")
                .has(GradleArguments.Foo::bar, Optional.of(4));
    }

    @Test
    void testPrint() {
        f.assertPrintsHelp(
                GradleArguments_FooParser.createModel(),
                "USAGE",
                "  foo [OPTIONS]",
                "",
                "OPTIONS",
                "  --bar BAR ",
                "");
    }
}
