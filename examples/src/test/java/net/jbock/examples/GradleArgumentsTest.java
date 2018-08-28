package net.jbock.examples;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

class GradleArgumentsTest {

  private ParserTestFixture<GradleArguments> f =
      ParserTestFixture.create(GradleArguments_Parser.newBuilder());

  @Test
  void errorShortLongConflict() {
    f.assertThat("-m", "hello", "--message=goodbye").failsWithLine1(
        "Option MESSAGE (-m, --message) is not repeatable");
  }

  @Test
  void errorMissingValue() {
    // there's nothing after -m
    f.assertThat("-m").failsWithLine1("Missing value after token: -m");
  }

  @Test
  void errorLongShortConflict() {
    f.assertThat("--message=hello", "-m", "goodbye").failsWithLine1(
        "Option MESSAGE (-m, --message) is not repeatable");
  }

  @Test
  void errorLongLongConflict() {
    f.assertThat("--message=hello", "--message=goodbye").failsWithLine1(
        "Option MESSAGE (-m, --message) is not repeatable");
  }

  @Test
  void errorInvalidOption() {
    f.assertThat("-c1").failsWithLine1("Invalid option: -c1");
    f.assertThat("-c-v").failsWithLine1("Invalid option: -c-v");
    f.assertThat("-c-").failsWithLine1("Invalid option: -c-");
    f.assertThat("-c=v").failsWithLine1("Invalid option: -c=v");
    f.assertThat("-c=").failsWithLine1("Invalid option: -c=");
    f.assertThat("-cX=1").failsWithLine1("Invalid option: -cX=1");
    f.assertThat("-cvv").failsWithLine1("Invalid option: -cvv");
    f.assertThat("-cvx").failsWithLine1("Invalid option: -cvx");
    f.assertThat("-cvm").failsWithLine1("Invalid option: -cvm");
  }

  @Test
  void testDetachedLong() {
    f.assertThat("--message", "hello").succeeds(
        "dir", null,
        "cmos", false,
        "verbose", false,
        "message", "hello",
        "file", emptyList(),
        "otherTokens", emptyList(),
        "ddTokens", emptyList());
  }

  @Test
  void testInterestingTokens() {
    f.assertThat("--message=hello", "b-a-b-a", "--", "->", "<=>", "", " ").succeeds(
        "dir", null,
        "cmos", false,
        "verbose", false,
        "message", "hello",
        "otherTokens", singletonList("b-a-b-a"),
        "ddTokens", asList("->", "<=>", "", " "),
        "file", emptyList());
  }

  @Test
  void testPassEmptyString() {
    f.assertThat("-m", "").succeeds(
        "dir", null,
        "cmos", false,
        "verbose", false,
        "message", "",
        "file", emptyList(),
        "otherTokens", emptyList(),
        "ddTokens", emptyList());
    f.assertThat("--message=").succeeds(
        "dir", null,
        "cmos", false,
        "verbose", false,
        "message", "",
        "file", emptyList(),
        "otherTokens", emptyList(),
        "ddTokens", emptyList());
    f.assertThat("--message", "").succeeds(
        "dir", null,
        "cmos", false,
        "verbose", false,
        "message", "",
        "file", emptyList(),
        "otherTokens", emptyList(),
        "ddTokens", emptyList());
  }

  @Test
  void testAllForms() {
    Object[] expectation = {
        "ddTokens", emptyList(),
        "otherTokens", emptyList(),
        "file", emptyList(),
        "message", "hello",
        "dir", null,
        "cmos", false,
        "verbose", false};
    f.assertThat("-mhello").succeeds(expectation);
    f.assertThat("-m", "hello").succeeds(expectation);
    f.assertThat("--message=hello").succeeds(expectation);
    f.assertThat("--message", "hello").succeeds(expectation);
  }

  @Test
  void testRepeatableShortAttached() {
    f.assertThat("-fbar.txt").succeeds(
        "message", null,
        "dir", null,
        "cmos", false,
        "verbose", false,
        "file", singletonList("bar.txt"),
        "otherTokens", emptyList(),
        "ddTokens", emptyList());
    f.assertThat("-fbar.txt", "--message=hello").succeeds(
        "message", "hello",
        "dir", null,
        "cmos", false,
        "verbose", false,
        "file", singletonList("bar.txt"),
        "otherTokens", emptyList(),
        "ddTokens", emptyList());
    f.assertThat("--message=hello", "-fbar.txt").succeeds(
        "message", "hello",
        "dir", null,
        "cmos", false,
        "verbose", false,
        "file", singletonList("bar.txt"),
        "otherTokens", emptyList(),
        "ddTokens", emptyList());
  }

  @Test
  void testLongSuppressed() {
    // Long option --cmos is suppressed
    f.assertThat("--cmos").failsWithLine1("Invalid option: --cmos");
  }

  @Test
  void testFlag() {
    f.assertThat("-c", "hello").succeeds(
        "ddTokens", emptyList(),
        "file", emptyList(),
        "cmos", true,
        "verbose", false,
        "dir", null,
        "message", null,
        "otherTokens", singletonList("hello"));
  }

  @Test
  void testPositionalOnly() {
    f.assertThat("hello", "goodbye").succeeds(
        "dir", null,
        "cmos", false,
        "verbose", false,
        "message", null,
        "otherTokens", asList("hello", "goodbye"),
        "file", emptyList(),
        "ddTokens", emptyList());
  }

  @Test
  void twoFlags() {
    f.assertThat("-c", "-v").succeeds(
        "dir", null,
        "cmos", true,
        "verbose", true,
        "message", null,
        "file", emptyList(),
        "otherTokens", emptyList(),
        "ddTokens", emptyList());
  }

  @Test
  void errorSuspiciousInput() {
    f.assertThat("-cvm", "hello").failsWithLine1("Invalid option: -cvm");
  }


  @Test
  void testPrint() {
    f.assertPrints(
        "NAME",
        "  GradleArguments",
        "",
        "SYNOPSIS",
        "  GradleArguments [OPTION]... [OTHER_TOKENS]... [-- DD_TOKENS...]",
        "",
        "DESCRIPTION",
        "",
        "  -m, --message MESSAGE",
        "    the message",
        "    message goes here",
        "",
        "  -f, --file FILE",
        "    the files",
        "",
        "  --dir DIR",
        "    the dir",
        "",
        "  -c",
        "    cmos flag",
        "",
        "  -v, --verbose",
        "",
        "");
  }
}
