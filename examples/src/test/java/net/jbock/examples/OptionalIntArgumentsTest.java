package net.jbock.examples;

import net.jbock.either.Either;
import net.jbock.examples.fixture.ParserTestFixture;
import net.jbock.util.HelpRequested;
import net.jbock.util.NotSuccess;
import org.junit.jupiter.api.Test;

import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertTrue;

class OptionalIntArgumentsTest {

  private final OptionalIntArgumentsParser parser = new OptionalIntArgumentsParser();

  private final ParserTestFixture<OptionalIntArguments> f =
      ParserTestFixture.create(parser::parse);

  @Test
  void testPresent() {
    f.assertThat("-a", "1").has(OptionalIntArguments::a, OptionalInt.of(1));
  }

  @Test
  void testAbsent() {
    Either<NotSuccess, OptionalIntArguments> result = parser.parse(/* empty */);
    assertTrue(result.getLeft().isPresent());
    result.acceptLeft(l -> assertTrue(l instanceof HelpRequested));
  }
}
