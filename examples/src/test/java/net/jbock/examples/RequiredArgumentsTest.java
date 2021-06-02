package net.jbock.examples;

import net.jbock.either.Either;
import net.jbock.examples.fixture.ParserTestFixture;
import net.jbock.util.HelpRequested;
import net.jbock.util.NotSuccess;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static java.util.Collections.emptyList;

class RequiredArgumentsTest {

  private final ParserTestFixture<RequiredArguments> f =
      ParserTestFixture.create(new RequiredArgumentsParser());

  @Test
  void success() {
    f.assertThat("--dir", "A").succeeds("dir", "A", "otherTokens", emptyList());
  }

  @Test
  void errorDirMissing() {
    Either<NotSuccess, RequiredArguments> result = new RequiredArgumentsParser().parse(new String[0]);
    Assertions.assertTrue(result.getLeft().isPresent());
    Assertions.assertTrue(result.getLeft().get() instanceof HelpRequested);
  }

  @Test
  void errorRepeatedArgument() {
    f.assertThat("--dir", "A", "--dir", "B").failsWithMessage(
        "Option '--dir' is a repetition");
    f.assertThat("--dir=A", "--dir", "B").failsWithMessage(
        "Option '--dir' is a repetition");
    f.assertThat("--dir=A", "--dir=B").failsWithMessage(
        "Option '--dir=B' is a repetition");
    f.assertThat("--dir", "A", "--dir=B").failsWithMessage(
        "Option '--dir=B' is a repetition");
  }

  @Test
  void errorDetachedAttached() {
    f.assertThat("--dir", "A", "--dir=B").failsWithMessage("Option '--dir=B' is a repetition");
  }

  @Test
  void testPrint() {
    f.assertPrintsHelp(
        "USAGE",
        "  required-arguments --dir DIR [OTHER_TOKENS]...",
        "",
        "PARAMETERS",
        "  OTHER_TOKENS ",
        "",
        "OPTIONS",
        "  --dir DIR ",
        "");
  }
}
