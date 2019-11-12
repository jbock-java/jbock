package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

class GradleArgumentsTest {

  private ParserTestFixture<GradleArguments> f =
      ParserTestFixture.create(GradleArguments_Parser.create());

  @Test
  void errorShortLongConflict() {
    f.assertThat("-m", "hello", "--message=goodbye").failsWithUsageMessage(
        "Option MESSAGE (-m, --message) is not repeatable");
  }

  @Test
  void errorMissingValue() {
    // there's nothing after -m
    f.assertThat("-m").failsWithUsageMessage("Missing value after token: -m");
  }

  @Test
  void errorLongShortConflict() {
    f.assertThat("--message=hello", "-m", "goodbye").failsWithUsageMessage(
        "Option MESSAGE (-m, --message) is not repeatable");
  }

  @Test
  void errorLongLongConflict() {
    f.assertThat("--message=hello", "--message=goodbye").failsWithUsageMessage(
        "Option MESSAGE (-m, --message) is not repeatable");
  }

  @Test
  void errorInvalidOption() {
    f.assertThat("-c1").failsWithUsageMessage("Invalid option: -c1");
    f.assertThat("-c-v").failsWithUsageMessage("Invalid option: -c-v");
    f.assertThat("-c-").failsWithUsageMessage("Invalid option: -c-");
    f.assertThat("-c=v").failsWithUsageMessage("Invalid option: -c=v");
    f.assertThat("-c=").failsWithUsageMessage("Invalid option: -c=");
    f.assertThat("-cX=1").failsWithUsageMessage("Invalid option: -cX=1");
    f.assertThat("-cvv").failsWithUsageMessage("Invalid option: -cvv");
    f.assertThat("-cvx").failsWithUsageMessage("Invalid option: -cvx");
    f.assertThat("-cvm").failsWithUsageMessage("Invalid option: -cvm");
  }

  @Test
  void testDetachedLong() {
    f.assertThat("--message", "hello").succeeds(
        "message", Optional.of("hello"),
        "file", emptyList(),
        "dir", Optional.empty(),
        "cmos", false,
        "verbose", false,
        "otherTokens", emptyList());
  }

  @Test
  void testInterestingTokens() {
    f.assertThat("--message=hello", "b-a-b-a", "--", "->", "<=>", "", " ").succeeds(
        "message", Optional.of("hello"),
        "file", emptyList(),
        "dir", Optional.empty(),
        "cmos", false,
        "verbose", false,
        "otherTokens", asList("b-a-b-a", "->", "<=>", "", " "));
  }

  @Test
  void testPassEmptyString() {
    f.assertThat("-m", "").succeeds(
        "message", Optional.of(""),
        "file", emptyList(),
        "dir", Optional.empty(),
        "cmos", false,
        "verbose", false,
        "otherTokens", emptyList());
    f.assertThat("--message=").succeeds(
        "message", Optional.of(""),
        "file", emptyList(),
        "dir", Optional.empty(),
        "cmos", false,
        "verbose", false,
        "otherTokens", emptyList());
    f.assertThat("--message", "").succeeds(
        "message", Optional.of(""),
        "file", emptyList(),
        "dir", Optional.empty(),
        "cmos", false,
        "verbose", false,
        "otherTokens", emptyList());
  }

  @Test
  void testAllForms() {
    Object[] expectation = {
        "message", Optional.of("hello"),
        "file", emptyList(),
        "dir", Optional.empty(),
        "cmos", false,
        "verbose", false,
        "otherTokens", emptyList()};
    f.assertThat("-mhello").succeeds(expectation);
    f.assertThat("-m", "hello").succeeds(expectation);
    f.assertThat("--message=hello").succeeds(expectation);
    f.assertThat("--message", "hello").succeeds(expectation);
  }

  @Test
  void testRepeatableShortAttached() {
    f.assertThat("-fbar.txt").succeeds(
        "message", Optional.empty(),
        "file", singletonList("bar.txt"),
        "dir", Optional.empty(),
        "cmos", false,
        "verbose", false,
        "otherTokens", emptyList());
    f.assertThat("-fbar.txt", "--message=hello").succeeds(
        "message", Optional.of("hello"),
        "file", singletonList("bar.txt"),
        "dir", Optional.empty(),
        "cmos", false,
        "verbose", false,
        "otherTokens", emptyList());
    f.assertThat("--message=hello", "-fbar.txt").succeeds(
        "message", Optional.of("hello"),
        "file", singletonList("bar.txt"),
        "dir", Optional.empty(),
        "cmos", false,
        "verbose", false,
        "otherTokens", emptyList());
  }

  @Test
  void testLongSuppressed() {
    // Long option --cmos is suppressed
    f.assertThat("--cmos").failsWithUsageMessage("Invalid option: --cmos");
  }

  @Test
  void testFlag() {
    f.assertThat("-c", "hello").succeeds(
        "message", Optional.empty(),
        "file", emptyList(),
        "dir", Optional.empty(),
        "cmos", true,
        "verbose", false,
        "otherTokens", singletonList("hello"));
  }

  @Test
  void testPositionalOnly() {
    f.assertThat("hello", "goodbye").succeeds(
        "message", Optional.empty(),
        "file", emptyList(),
        "dir", Optional.empty(),
        "cmos", false,
        "verbose", false,
        "otherTokens", asList("hello", "goodbye"));
  }

  @Test
  void twoFlags() {
    f.assertThat("-c", "-v").succeeds(
        "message", Optional.empty(),
        "file", emptyList(),
        "dir", Optional.empty(),
        "cmos", true,
        "verbose", true,
        "otherTokens", emptyList());
  }

  @Test
  void errorSuspiciousInput() {
    f.assertThat("-cvm", "hello").failsWithUsageMessage("Invalid option: -cvm");
  }

  @Test
  void testPrint() {
    f.assertPrintsHelp(
        "NAME",
        "  GradleArguments",
        "",
        "SYNOPSIS",
        "  GradleArguments [OPTIONS...] [<other_tokens>...]",
        "",
        "DESCRIPTION",
        "",
        "OTHER_TOKENS",
        "",
        "OPTIONS",
        "  -m <message>, --message <message>",
        "    the message",
        "    message goes here",
        "",
        "  -f <file...>, --file <file...>",
        "    the files",
        "",
        "  --dir <dir>",
        "    the dir",
        "",
        "  -c",
        "    cmos flag",
        "",
        "  -v, --verbose",
        "",
        "  --help",
        "    Print this help page.",
        "    The help flag may only be passed as the first argument.",
        "    Any further arguments will be ignored.",
        "",
        "");
  }
}
