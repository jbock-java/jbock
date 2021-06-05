package net.jbock.examples;

import net.jbock.either.Either;
import net.jbock.examples.fixture.ParserTestFixture;
import net.jbock.util.ConverterError;
import net.jbock.util.NotSuccess;
import net.jbock.util.ParsingError;
import net.jbock.util.SyntaxError;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AllCharactersArgumentsTest {

  private final AllCharactersArgumentsParser parser = new AllCharactersArgumentsParser();

  private final ParserTestFixture<AllCharactersArguments> f =
      ParserTestFixture.create(parser);

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
    Either<NotSuccess, AllCharactersArguments> result = parser.parse(
        "--smallChar", "abc",
        "--bigChar", "A",
        "--charOpt", "X",
        "--charList", "b",
        "--charList", "c");
    assertTrue(result.getLeft().map(f::castToError).orElseThrow().message().contains(
        "while converting option SMALLCHAR (--smallChar): Not a single character: <abc>"));
  }
}
