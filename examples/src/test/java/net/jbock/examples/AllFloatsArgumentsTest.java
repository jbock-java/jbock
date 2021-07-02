package net.jbock.examples;

import net.jbock.either.Optional;
import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.List;

class AllFloatsArgumentsTest {

  private final AllFloatsArgumentsParser parser = new AllFloatsArgumentsParser();

  private final ParserTestFixture<AllFloatsArguments> f =
      ParserTestFixture.create(parser::parse);

  @Test
  void listOfInteger() {
    f.assertThat("-i1.5", "-i2.5", "-i2.5", "-i3.5", "--obj=1.5", "--prim=1.5")
        .has(AllFloatsArguments::positional, List.of())
        .has(AllFloatsArguments::listOfFloats, List.of(1.5f, 2.5f, 2.5f, 3.5f))
        .has(AllFloatsArguments::optionalFloat, Optional.empty())
        .has(AllFloatsArguments::floatObject, 1.5f)
        .has(AllFloatsArguments::primitiveFloat, 1.5f);
  }

  @Test
  void optionalInteger() {
    f.assertThat("--opt", "1.5", "--obj=1.5", "--prim=1.5")
        .has(AllFloatsArguments::positional, List.of())
        .has(AllFloatsArguments::listOfFloats, List.of())
        .has(AllFloatsArguments::optionalFloat, Optional.of(1.5f))
        .has(AllFloatsArguments::floatObject, 1.5f)
        .has(AllFloatsArguments::primitiveFloat, 1.5f);
  }

  @Test
  void positional() {
    f.assertThat("--obj=1.5", "--prim=1.5", "5.5", "3.5")
        .has(AllFloatsArguments::positional, List.of(5.5f, 3.5f))
        .has(AllFloatsArguments::listOfFloats, List.of())
        .has(AllFloatsArguments::optionalFloat, Optional.empty())
        .has(AllFloatsArguments::floatObject, 1.5f)
        .has(AllFloatsArguments::primitiveFloat, 1.5f);
  }
}