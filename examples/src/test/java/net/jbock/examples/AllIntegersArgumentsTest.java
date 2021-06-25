package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

class AllIntegersArgumentsTest {

  private final AllIntegersArgumentsParser parser = new AllIntegersArgumentsParser();

  private final ParserTestFixture<AllIntegersArguments> f =
      ParserTestFixture.create(parser::parse);

  @Test
  void listOfInteger() {
    f.assertThat("-i1", "-i2", "-i2", "-i3", "--obj=1", "--prim=1").succeeds(
        "positional", List.of(),
        "listOfIntegers", List.of(1, 2, 2, 3),
        "optionalInteger", Optional.empty(),
        "integer", 1,
        "primitiveInt", 1);
  }

  @Test
  void optionalInteger() {
    f.assertThat("--opt", "1", "--obj=1", "--prim=1").succeeds(
        "positional", List.of(),
        "listOfIntegers", List.of(),
        "optionalInteger", Optional.of(1),
        "integer", 1,
        "primitiveInt", 1);
  }

  @Test
  void positional() {
    f.assertThat("--obj=1", "--prim=1", "5", "3", "--opti=5").succeeds(
        "positional", List.of(5, 3),
        "listOfIntegers", List.of(),
        "optionalInteger", Optional.empty(),
        "integer", 1,
        "optionalInt", OptionalInt.of(5),
        "primitiveInt", 1);
  }
}
