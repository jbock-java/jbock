package net.jbock.examples;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

class GradleArgumentsTest {

  private ParserTestFixture<GradleArguments> f =
      ParserTestFixture.create(GradleArguments_Parser::parse);

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
        "message", "hello");
  }

  @Test
  void testInterestingTokens() {
    f.assertThat("--message=hello", "b-a-b-a", "--", "->", "<=>", "", " ").succeeds(
        "message", "hello",
        "otherTokens", singletonList("b-a-b-a"),
        "ddTokens", asList("->", "<=>", "", " "));
  }

  @Test
  void testPassEmptyString() {
    f.assertThat("-m", "").succeeds("message", "");
    f.assertThat("--message=").succeeds("message", "");
    f.assertThat("--message", "").succeeds("message", "");
  }

  @Test
  void testAllForms() {
    f.assertThat("-mhello").succeeds("message", "hello");
    f.assertThat("-m", "hello").succeeds("message", "hello");
    f.assertThat("--message=hello").succeeds("message", "hello");
    f.assertThat("--message", "hello").succeeds("message", "hello");
  }

  @Test
  void testRepeatableShortAttached() {
    f.assertThat("-fbar.txt").succeeds(
        "file", singletonList("bar.txt"));
    f.assertThat("-fbar.txt", "--message=hello").succeeds(
        "message", "hello",
        "file", singletonList("bar.txt"));
    f.assertThat("--message=hello", "-fbar.txt").succeeds(
        "message", "hello",
        "file", singletonList("bar.txt"));
  }

  @Test
  void testLongSuppressed() {
    // Long option --cmos is suppressed
    f.assertThat("--cmos").failsWithLine1("Invalid option: --cmos");
  }

  @Test
  void testFlag() {
    f.assertThat("-c", "hello").succeeds(
        "cmos", true,
        "otherTokens", singletonList("hello"));
  }

  @Test
  void testPositionalOnly() {
    f.assertThat("hello", "goodbye").succeeds(
        "otherTokens", asList("hello", "goodbye"));
  }

  @Test
  void twoFlags() {
    f.assertThat("-c", "-v").succeeds(
        "cmos", true,
        "verbose", true);
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
