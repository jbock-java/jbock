package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

class AllLongsArgumentsTest {

  private ParserTestFixture<AllLongsArguments> f =
      ParserTestFixture.create(AllLongsArguments_Parser.create());

  @Test
  void listOfInteger() {
    f.assertThat("-i1", "-i2", "-i2", "-i3", "--obj=1", "--prim=1").succeeds(
        "positional", emptyList(),
        "listOfLongs", asList(1, 2, 2, 3),
        "optionalLong", null,
        "optionalPrimitiveLong", null,
        "longObject", 1,
        "primitiveLong", 1);
  }

  @Test
  void optionalInteger() {
    f.assertThat("--opt", "1", "--obj=1", "--prim=1").succeeds(
        "positional", emptyList(),
        "listOfLongs", emptyList(),
        "optionalLong", 1,
        "optionalPrimitiveLong", null,
        "longObject", 1,
        "primitiveLong", 1);
  }

  @Test
  void positional() {
    f.assertThat("--obj=1", "--prim=1", "5", "3").succeeds(
        "positional", asList(5, 3),
        "listOfLongs", emptyList(),
        "optionalLong", null,
        "optionalPrimitiveLong", null,
        "longObject", 1,
        "primitiveLong", 1);
  }

}