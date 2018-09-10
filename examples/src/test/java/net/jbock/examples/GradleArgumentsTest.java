package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

class GradleArgumentsTest {

  private ParserTestFixture<GradleArguments> f =
      ParserTestFixture.create(GradleArguments_Parser.create());

  @Test
  void errorShortLongConflict() {
    f.assertThat("-m", "hello", "--message=goodbye").failsWithLine4(
        "Option MESSAGE (-m, --message) is not repeatable");
  }

  @Test
  void errorMissingValue() {
    // there's nothing after -m
    f.assertThat("-m").failsWithLine4("Missing value after token: -m");
  }

  @Test
  void errorLongShortConflict() {
    f.assertThat("--message=hello", "-m", "goodbye").failsWithLine4(
        "Option MESSAGE (-m, --message) is not repeatable");
  }

  @Test
  void errorLongLongConflict() {
    f.assertThat("--message=hello", "--message=goodbye").failsWithLine4(
        "Option MESSAGE (-m, --message) is not repeatable");
  }

  @Test
  void errorInvalidOption() {
    f.assertThat("-c1").failsWithLine4("Invalid option: -c1");
    f.assertThat("-c-v").failsWithLine4("Invalid option: -c-v");
    f.assertThat("-c-").failsWithLine4("Invalid option: -c-");
    f.assertThat("-c=v").failsWithLine4("Invalid option: -c=v");
    f.assertThat("-c=").failsWithLine4("Invalid option: -c=");
    f.assertThat("-cX=1").failsWithLine4("Invalid option: -cX=1");
    f.assertThat("-cvv").failsWithLine4("Invalid option: -cvv");
    f.assertThat("-cvx").failsWithLine4("Invalid option: -cvx");
    f.assertThat("-cvm").failsWithLine4("Invalid option: -cvm");
  }

  @Test
  void testDetachedLong() {
    f.assertThat("--message", "hello").succeeds(
        "message", "hello",
        "file", emptyList(),
        "dir", null,
        "cmos", false,
        "verbose", false,
        "otherTokens", emptyList());
  }

  @Test
  void testInterestingTokens() {
    f.assertThat("--message=hello", "b-a-b-a", "--", "->", "<=>", "", " ").succeeds(
        "message", "hello",
        "file", emptyList(),
        "dir", null,
        "cmos", false,
        "verbose", false,
        "otherTokens", asList("b-a-b-a", "->", "<=>", "", " "));
  }

  @Test
  void testPassEmptyString() {
    f.assertThat("-m", "").succeeds(
        "message", "",
        "file", emptyList(),
        "dir", null,
        "cmos", false,
        "verbose", false,
        "otherTokens", emptyList());
    f.assertThat("--message=").succeeds(
        "message", "",
        "file", emptyList(),
        "dir", null,
        "cmos", false,
        "verbose", false,
        "otherTokens", emptyList());
    f.assertThat("--message", "").succeeds(
        "message", "",
        "file", emptyList(),
        "dir", null,
        "cmos", false,
        "verbose", false,
        "otherTokens", emptyList());
  }

  @Test
  void testAllForms() {
    Object[] expectation = {
        "message", "hello",
        "file", emptyList(),
        "dir", null,
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
        "message", null,
        "file", singletonList("bar.txt"),
        "dir", null,
        "cmos", false,
        "verbose", false,
        "otherTokens", emptyList());
    f.assertThat("-fbar.txt", "--message=hello").succeeds(
        "message", "hello",
        "file", singletonList("bar.txt"),
        "dir", null,
        "cmos", false,
        "verbose", false,
        "otherTokens", emptyList());
    f.assertThat("--message=hello", "-fbar.txt").succeeds(
        "message", "hello",
        "file", singletonList("bar.txt"),
        "dir", null,
        "cmos", false,
        "verbose", false,
        "otherTokens", emptyList());
  }

  @Test
  void testLongSuppressed() {
    // Long option --cmos is suppressed
    f.assertThat("--cmos").failsWithLine4("Invalid option: --cmos");
  }

  @Test
  void testFlag() {
    f.assertThat("-c", "hello").succeeds(
        "message", null,
        "file", emptyList(),
        "dir", null,
        "cmos", true,
        "verbose", false,
        "otherTokens", singletonList("hello"));
  }

  @Test
  void testPositionalOnly() {
    f.assertThat("hello", "goodbye").succeeds(
        "message", null,
        "file", emptyList(),
        "dir", null,
        "cmos", false,
        "verbose", false,
        "otherTokens", asList("hello", "goodbye"));
  }

  @Test
  void twoFlags() {
    f.assertThat("-c", "-v").succeeds(
        "message", null,
        "file", emptyList(),
        "dir", null,
        "cmos", true,
        "verbose", true,
        "otherTokens", emptyList());
  }

  @Test
  void errorSuspiciousInput() {
    f.assertThat("-cvm", "hello").failsWithLine4("Invalid option: -cvm");
  }

  @Test
  void testPrint() {
    f.assertPrints(
        "NAME",
        "  GradleArguments",
        "",
        "SYNOPSIS",
        "  GradleArguments [<options>] [[--] <other_tokens...>]",
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
