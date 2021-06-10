package net.jbock.examples;

import net.jbock.either.Either;
import net.jbock.util.NotSuccess;
import net.jbock.util.SuperResult;
import net.jbock.util.ErrToken;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HelplessSuperArgumentsTest {

  private final HelplessSuperArgumentsParser parser = new HelplessSuperArgumentsParser();

  @Test
  void testHelpDisabled() {
    Either<NotSuccess, SuperResult<HelplessSuperArguments>> result = parser.parse("--help");
    assertTrue(result.getLeft().isPresent());
    assertTrue(result.getLeft().get() instanceof ErrToken);
  }
}
