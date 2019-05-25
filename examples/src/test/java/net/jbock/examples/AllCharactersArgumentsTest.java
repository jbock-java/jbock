package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static java.util.Arrays.asList;

class AllCharactersArgumentsTest {

  private ParserTestFixture<AllCharactersArguments> f =
      ParserTestFixture.create(AllCharactersArguments_Parser.create());

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
        "--charList", "c").failsWithUsageMessage(
        "Not a character: <abc>");
  }
}