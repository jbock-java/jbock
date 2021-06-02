package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static java.util.Arrays.asList;

class AllCharactersArgumentsTest {

  private final ParserTestFixture<AllCharactersArguments> f =
      ParserTestFixture.create(new AllCharactersArgumentsParser());

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
        "charList", asList('b', 'c'));
  }

  @Test
  void fail() {
    f.assertThat(
        "--smallChar", "abc",
        "--bigChar", "A",
        "--charOpt", "X",
        "--charList", "b",
        "--charList", "c").failsWithMessage(
        "while converting option SMALLCHAR (--smallChar): Not a single character: <abc>");
  }
}
