package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

class AllIntegersArgumentsTest {

    private final ParserTestFixture<AllIntegersArguments> f =
            ParserTestFixture.create(AllIntegersArgumentsParser::parse);

    @Test
    void listOfInteger() {
        f.assertThat("-i1", "-i2", "-i2", "-i3", "--obj=1", "--prim=1")
                .has(AllIntegersArguments::positional, List.of())
                .has(AllIntegersArguments::listOfIntegers, List.of(1, 2, 2, 3))
                .has(AllIntegersArguments::optionalInteger, Optional.empty())
                .has(AllIntegersArguments::integer, 1)
                .has(AllIntegersArguments::primitiveInt, 1);
    }

    @Test
    void optionalInteger() {
        f.assertThat("--opt", "1", "--obj=1", "--prim=1")
                .has(AllIntegersArguments::positional, List.of())
                .has(AllIntegersArguments::listOfIntegers, List.of())
                .has(AllIntegersArguments::optionalInteger, Optional.of(1))
                .has(AllIntegersArguments::integer, 1)
                .has(AllIntegersArguments::optionalInt, OptionalInt.empty())
                .has(AllIntegersArguments::primitiveInt, 1);
    }

    @Test
    void positional() {
        f.assertThat("--obj=1", "--prim=1", "5", "3", "--opti=5")
                .has(AllIntegersArguments::positional, List.of(5, 3))
                .has(AllIntegersArguments::listOfIntegers, List.of())
                .has(AllIntegersArguments::optionalInteger, Optional.empty())
                .has(AllIntegersArguments::integer, 1)
                .has(AllIntegersArguments::optionalInt, OptionalInt.of(5))
                .has(AllIntegersArguments::primitiveInt, 1);
    }
}
