package net.jbock.examples;

import net.jbock.examples.RequiredArguments_Parser.ParseResult;
import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequiredArgumentsTest {

  private final ParserTestFixture<RequiredArguments> f =
      ParserTestFixture.create(new RequiredArguments_Parser());

  @Test
  void success() {
    f.assertThat("--dir", "A").succeeds("dir", "A", "otherTokens", emptyList());
  }

  @Test
  void errorDirMissing() {
    ParseResult result = new RequiredArguments_Parser().parse(new String[0]);
    assertTrue(result instanceof RequiredArguments_Parser.HelpRequested);
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
