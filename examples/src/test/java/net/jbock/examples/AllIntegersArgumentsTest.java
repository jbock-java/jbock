package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

class AllIntegersArgumentsTest {

  private ParserTestFixture<AllIntegersArguments> f =
      ParserTestFixture.create(AllIntegersArguments_Parser.create());

  @Test
  void listOfInteger() {
    f.assertThat("-i1", "-i2", "-i2", "-i3", "--obj=1", "--prim=1").succeeds(
        "positional", emptyList(),
        "listOfIntegers", asList(1, 2, 2, 3),
        "optionalInteger", null,
        "optionalInt", null,
        "integer", 1,
        "primitiveInt", 1);
  }

  @Test
  void optionalInteger() {
    f.assertThat("--opt", "1", "--obj=1", "--prim=1").succeeds(
        "positional", emptyList(),
        "listOfIntegers", emptyList(),
        "optionalInteger", 1,
        "optionalInt", null,
        "integer", 1,
        "primitiveInt", 1);
  }

  @Test
  void positional() {
    f.assertThat("--obj=1", "--prim=1", "5", "3").succeeds(
        "positional", asList(5, 3),
        "listOfIntegers", emptyList(),
        "optionalInteger", null,
        "optionalInt", null,
        "integer", 1,
        "primitiveInt", 1);
  }
}