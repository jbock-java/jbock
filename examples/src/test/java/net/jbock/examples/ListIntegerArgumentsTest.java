package net.jbock.examples;

import net.jbock.either.Either;
import net.jbock.util.HelpRequested;
import net.jbock.util.NotSuccess;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ListIntegerArgumentsTest {

  private final ListIntegerArgumentsParser parser = new ListIntegerArgumentsParser();

  @Test
  void testPresent() {
    ListIntegerArguments args = parser.parse("-a", "1")
        .orElseThrow(l -> Assertions.<RuntimeException>fail("expecting success but found: " + l));
    assertEquals(Collections.singletonList(1), args.a());
  }

  @Test
  void testAbsent() {
    String[] emptyInput = {};
    Either<NotSuccess, ListIntegerArguments> result = parser.parse(emptyInput);
    assertTrue(result.getLeft().isPresent());
    assertTrue(result.getLeft().get() instanceof HelpRequested);
  }
}