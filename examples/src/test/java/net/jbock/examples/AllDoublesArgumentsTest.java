package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

class AllDoublesArgumentsTest {

  private final AllDoublesArgumentsParser parser = new AllDoublesArgumentsParser();

  private final ParserTestFixture<AllDoublesArguments> f =
      ParserTestFixture.create(parser::parse);

  @Test
  void listOfInteger() {
    f.assertThat("-i1.5", "-i2.5", "-i2.5", "-i3.5", "--obj=1.5", "--prim=1.5").succeeds(
        "positional", List.of(),
        "listOfDoubles", List.of(1.5d, 2.5d, 2.5d, 3.5d),
        "optionalDouble", Optional.empty(),
        "doubleObject", 1.5d,
        "primitiveDouble", 1.5d);
  }

  @Test
  void optionalInteger() {
    f.assertThat("--opt", "1.5", "--obj=1.5", "--prim=1.5").succeeds(
        "positional", List.of(),
        "listOfDoubles", List.of(),
        "optionalDouble", Optional.of(1.5d),
        "doubleObject", 1.5d,
        "primitiveDouble", 1.5d);
  }

  @Test
  void positional() {
    f.assertThat("--obj=1.5", "--prim=1.5", "5.5", "3.5").succeeds(
        "positional", List.of(5.5d, 3.5d),
        "listOfDoubles", List.of(),
        "optionalDouble", Optional.empty(),
        "doubleObject", 1.5d,
        "primitiveDouble", 1.5d);
  }
}
