package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

class AllLongsArgumentsTest {

    private final ParserTestFixture<AllLongsArguments> f =
            ParserTestFixture.create(AllLongsArgumentsParser::parse);

    @Test
    void listOfInteger() {
        f.assertThat("-i1", "-i2", "-i2", "-i3", "--obj=1", "--prim=1")
                .has(AllLongsArguments::positional, List.of())
                .has(AllLongsArguments::listOfLongs, List.of(1L, 2L, 2L, 3L))
                .has(AllLongsArguments::optionalLong, Optional.empty())
                .has(AllLongsArguments::longObject, 1L)
                .has(AllLongsArguments::primitiveLong, 1L);
    }

    @Test
    void optionalInteger() {
        f.assertThat("--opt", "1", "--obj=1", "--prim=1")
                .has(AllLongsArguments::positional, List.of())
                .has(AllLongsArguments::listOfLongs, List.of())
                .has(AllLongsArguments::optionalLong, Optional.of(1L))
                .has(AllLongsArguments::longObject, 1L)
                .has(AllLongsArguments::primitiveLong, 1L);
    }

    @Test
    void positional() {
        f.assertThat("--obj=1", "--prim=1", "5", "3")
                .has(AllLongsArguments::positional, List.of(5L, 3L))
                .has(AllLongsArguments::listOfLongs, List.of())
                .has(AllLongsArguments::optionalLong, Optional.empty())
                .has(AllLongsArguments::longObject, 1L)
                .has(AllLongsArguments::primitiveLong, 1L);
    }
}
