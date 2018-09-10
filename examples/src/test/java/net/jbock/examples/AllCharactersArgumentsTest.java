package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

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
        "charOpt", 'X',
        "charList", asList('b', 'c'));
  }

  @Test
  void fail() {
    f.assertThat(
        "--smallChar", "abc",
        "--bigChar", "A",
        "--charOpt", "X",
        "--charList", "b",
        "--charList", "c").failsWithLine4(
        "Not a character: <abc>");
  }
}