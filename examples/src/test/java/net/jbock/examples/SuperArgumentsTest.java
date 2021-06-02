package net.jbock.examples;

import net.jbock.either.Either;
import net.jbock.util.HelpRequested;
import net.jbock.util.NotSuccess;
import net.jbock.util.SuperResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SuperArgumentsTest {

  private final SuperArgumentsParser parser = new SuperArgumentsParser();

  @Test
  void testRest() {
    SuperResult<SuperArguments> success = parseOrFail("-q", "foo", "-a", "1");
    SuperArguments result = success.result();
    assertEquals("foo", result.command());
    assertTrue(result.quiet());
    assertArrayEquals(new String[]{"-a", "1"}, success.rest());
  }

  @Test
  void testEscapeSequenceNotRecognized() {
    SuperResult<SuperArguments> success = parseOrFail("-q", "--");
    SuperArguments result = success.result();
    assertEquals("--", result.command());
    assertTrue(result.quiet());
  }

  @Test
  void testHelp() {
    Either<NotSuccess, SuperResult<SuperArguments>> result = parser.parse(new String[]{"--help"});
    assertTrue(result.getLeft().isPresent());
    assertTrue(result.getLeft().get() instanceof HelpRequested);
  }

  private SuperResult<SuperArguments> parseOrFail(String... args) {
    return parser.parse(args)
        .orElseThrow(notSuccess -> Assertions.<RuntimeException>fail("success expected but was " + notSuccess));
  }
}
