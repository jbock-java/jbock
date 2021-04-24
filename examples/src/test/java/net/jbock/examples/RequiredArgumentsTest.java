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
        "Option DIR (--dir) is not repeatable");
    f.assertThat("--dir=A", "--dir", "B").failsWithMessage(
        "Option DIR (--dir) is not repeatable");
    f.assertThat("--dir=A", "--dir=B").failsWithMessage(
        "Option DIR (--dir) is not repeatable");
    f.assertThat("--dir", "A", "--dir=B").failsWithMessage(
        "Option DIR (--dir) is not repeatable");
  }

  @Test
  void errorDetachedAttached() {
    f.assertThat("--dir", "A", "--dir=B").failsWithMessage("Option DIR (--dir) is not repeatable");
  }

  @Test
  void testPrint() {
    f.assertPrintsHelp(
        "Usage: required-arguments --dir <dir> <other_tokens>...",
        "  other_tokens  ",
        "  --dir DIR     ",
        "");
  }
}
