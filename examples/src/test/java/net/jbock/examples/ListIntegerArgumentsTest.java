package net.jbock.examples;

import net.jbock.either.Either;
import net.jbock.util.HelpRequested;
import net.jbock.util.NotSuccess;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ListIntegerArgumentsTest {

  @Test
  void testPresent() {
    ListIntegerArguments args = new ListIntegerArgumentsParser().parseOrExit(new String[]{"-a", "1"});
    assertEquals(Collections.singletonList(1), args.a());
  }

  @Test
  void testAbsent() {
    Either<NotSuccess, ListIntegerArguments> result = new ListIntegerArgumentsParser().parse(new String[]{});
    assertTrue(result.getLeft().isPresent());
    assertTrue(result.getLeft().get() instanceof HelpRequested);
  }
}