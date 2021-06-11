package net.jbock.examples;

import net.jbock.either.Either;
import net.jbock.util.HelpRequested;
import net.jbock.util.NotSuccess;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OptionalIntArgumentsTest {

  private final OptionalIntArgumentsParser parser = new OptionalIntArgumentsParser();

  @Test
  void testPresent() {
    OptionalIntArguments args = parser.parse("-a", "1")
        .orElseThrow(l -> Assertions.<RuntimeException>fail("expecting success but found: " + l));
    assertEquals(OptionalInt.of(1), args.a());
  }

  @Test
  void testAbsent() {
    String[] emptyInput = {};
    Either<NotSuccess, OptionalIntArguments> result = parser.parse(emptyInput);
    assertTrue(result.getLeft().isPresent());
    assertTrue(result.getLeft().get() instanceof HelpRequested);
  }
}
