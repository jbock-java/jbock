package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

class AllDoublesArgumentsTest {

  private ParserTestFixture<AllDoublesArguments> f =
      ParserTestFixture.create(new AllDoublesArguments_Parser());

  @Test
  void listOfInteger() {
    f.assertThat("-i1.5", "-i2.5", "-i2.5", "-i3.5", "--obj=1.5", "--prim=1.5").succeeds(
        "positional", emptyList(),
        "listOfDoubles", asList(1.5d, 2.5d, 2.5d, 3.5d),
        "optionalDouble", Optional.empty(),
        "doubleObject", 1.5d,
        "primitiveDouble", 1.5d);
  }

  @Test
  void optionalInteger() {
    f.assertThat("--opt", "1.5", "--obj=1.5", "--prim=1.5").succeeds(
        "positional", emptyList(),
        "listOfDoubles", emptyList(),
        "optionalDouble", Optional.of(1.5d),
        "doubleObject", 1.5d,
        "primitiveDouble", 1.5d);
  }

  @Test
  void positional() {
    f.assertThat("--obj=1.5", "--prim=1.5", "5.5", "3.5").succeeds(
        "positional", asList(5.5d, 3.5d),
        "listOfDoubles", emptyList(),
        "optionalDouble", Optional.empty(),
        "doubleObject", 1.5d,
        "primitiveDouble", 1.5d);
  }
}
