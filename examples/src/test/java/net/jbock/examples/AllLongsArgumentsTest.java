package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

class AllLongsArgumentsTest {

  private final AllLongsArgumentsParser parser = new AllLongsArgumentsParser();

  private final ParserTestFixture<AllLongsArguments> f =
      ParserTestFixture.create(parser::parse);

  @Test
  void listOfInteger() {
    f.assertThat("-i1", "-i2", "-i2", "-i3", "--obj=1", "--prim=1").succeeds(
        "positional", List.of(),
        "listOfLongs", List.of(1L, 2L, 2L, 3L),
        "optionalLong", Optional.empty(),
        "longObject", 1L,
        "primitiveLong", 1L);
  }

  @Test
  void optionalInteger() {
    f.assertThat("--opt", "1", "--obj=1", "--prim=1").succeeds(
        "positional", List.of(),
        "listOfLongs", List.of(),
        "optionalLong", Optional.of(1L),
        "longObject", 1L,
        "primitiveLong", 1L);
  }

  @Test
  void positional() {
    f.assertThat("--obj=1", "--prim=1", "5", "3").succeeds(
        "positional", List.of(5L, 3L),
        "listOfLongs", List.of(),
        "optionalLong", Optional.empty(),
        "longObject", 1L,
        "primitiveLong", 1L);
  }
}
