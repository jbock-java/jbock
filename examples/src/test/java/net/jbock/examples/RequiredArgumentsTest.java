package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import static java.util.Collections.emptyList;

class RequiredArgumentsTest {

  private final ParserTestFixture<RequiredArguments> f =
      ParserTestFixture.create(new RequiredArguments_Parser());

  @Test
  void success() {
    f.assertThat("--dir", "A").succeeds("dir", "A", "otherTokens", emptyList());
  }

  @Test
  void errorDirMissing() {
    f.assertThat().failsWithMessage("Missing required: DIR (--dir)");
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
        "  required-arguments --dir <dir> <other_tokens>...",
        "",
        "PARAMETERS",
        "  other_tokens ",
        "",
        "OPTIONS",
        "  --dir DIR    ",
        "");
  }
}
