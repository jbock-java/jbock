package net.jbock.examples;

import net.jbock.either.Either;
import net.jbock.util.HelpRequested;
import net.jbock.util.NotSuccess;
import org.junit.jupiter.api.Test;

import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OptionalIntArgumentsTest {

  @Test
  void testPresent() {
    OptionalIntArguments args = new OptionalIntArgumentsParser().parseOrExit(new String[]{"-a", "1"});
    assertEquals(OptionalInt.of(1), args.a());
  }

  @Test
  void testAbsent() {
    Either<NotSuccess, OptionalIntArguments> result = new OptionalIntArgumentsParser().parse(new String[]{});
    assertTrue(result.getLeft().isPresent());
    assertTrue(result.getLeft().get() instanceof HelpRequested);
  }
}
