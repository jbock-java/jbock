package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import static java.util.Collections.emptyList;

class RequiredArgumentsTest {

  private ParserTestFixture<RequiredArguments> f =
      ParserTestFixture.create(RequiredArguments_Parser.create());

  @Test
  void success() {
    f.assertThat("--dir", "A").succeeds("dir", "A", "otherTokens", emptyList());
  }

  @Test
  void errorDirMissing() {
    f.assertThat().failsWithUsageMessage("Missing required option: DIR (--dir)");
  }

  @Test
  void errorRepeatedArgument() {
    f.assertThat("--dir", "A", "--dir", "B").failsWithUsageMessage(
        "Option DIR (--dir) is not repeatable");
    f.assertThat("--dir=A", "--dir", "B").failsWithUsageMessage(
        "Option DIR (--dir) is not repeatable");
    f.assertThat("--dir=A", "--dir=B").failsWithUsageMessage(
        "Option DIR (--dir) is not repeatable");
    f.assertThat("--dir", "A", "--dir=B").failsWithUsageMessage(
        "Option DIR (--dir) is not repeatable");
  }

  @Test
  void errorDetachedAttached() {
    f.assertThat("--dir", "A", "--dir=B").failsWithUsageMessage("Option DIR (--dir) is not repeatable");
  }

  @Test
  void testPrint() {
    f.assertPrintsHelp(
        "NAME",
        "  RequiredArguments",
        "",
        "SYNOPSIS",
        "  RequiredArguments --dir=<DIR> [<other_tokens>...]",
        "",
        "DESCRIPTION",
        "",
        "OTHER_TOKENS",
        "",
        "OPTIONS",
        "  --dir <DIR>",
        "",
        "  --help",
        "    Print this help page.",
        "    The help flag may only be passed as the first argument.",
        "    Any further arguments will be ignored.",
        "",
        "");
  }
}
