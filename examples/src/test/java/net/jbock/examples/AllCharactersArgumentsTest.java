package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

class AllCharactersArgumentsTest {

  private final AllCharactersArgumentsParser parser = new AllCharactersArgumentsParser();

  private final ParserTestFixture<AllCharactersArguments> f =
      ParserTestFixture.create(parser::parse);

  @Test
  void tests() {
    f.assertThat(
        "--smallChar", "a",
        "--bigChar", "A",
        "--charOpt", "X",
        "--charList", "b",
        "--charList", "c").succeeds(
        "smallChar", 'a',
        "bigChar", 'A',
        "charOpt", Optional.of('X'),
        "charList", List.of('b', 'c'));
  }

  @Test
  void fail() {
    f.assertThat(
        "--smallChar", "abc",
        "--bigChar", "A",
        "--charOpt", "X",
        "--charList", "b",
        "--charList", "c").fails(
        "while converting option SMALLCHAR (--smallChar): Not a single character: <abc>");
  }
}
