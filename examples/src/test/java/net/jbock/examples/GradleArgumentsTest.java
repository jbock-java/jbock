package net.jbock.examples;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import net.jbock.examples.fixture.ParserFixture;
import org.junit.Test;

public final class GradleArgumentsTest {

  private final ParserFixture<GradleArguments> f =
      ParserFixture.create(GradleArguments_Parser::parse);

  @Test
  public void errorShortLongConflict() {
    f.assertThat("-m", "hello", "--message=goodbye").failsWithLine1(
        "Option MESSAGE (-m, --message) is not repeatable");
  }

  @Test
  public void errorMissingValue() {
    // there's nothing after -m
    f.assertThat("-m").failsWithLine1("Missing value after token: -m");
  }

  @Test
  public void errorLongShortConflict() {
    f.assertThat("--message=hello", "-m", "goodbye").failsWithLine1(
        "Option MESSAGE (-m, --message) is not repeatable");
  }

  @Test
  public void errorLongLongConflict() {
    f.assertThat("--message=hello", "--message=goodbye").failsWithLine1(
        "Option MESSAGE (-m, --message) is not repeatable");
  }

  @Test
  public void errorInvalidOption() {
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
  public void testDetachedLong() {
    f.assertThat("--message", "hello").parsesTo(
        "message", "hello");
  }

  @Test
  public void testInterestingTokens() {
    f.assertThat("--message=hello", "b-a-b-a", "--", "->", "<=>", "", " ").parsesTo(
        "message", "hello",
        "otherTokens", singletonList("b-a-b-a"),
        "ddTokens", asList("->", "<=>", "", " "));
  }

  @Test
  public void testPassEmptyString() {
    f.assertThat("-m", "").parsesTo("message", "");
    f.assertThat("--message=").parsesTo("message", "");
    f.assertThat("--message", "").parsesTo("message", "");
  }

  @Test
  public void testAllForms() {
    f.assertThat("-mhello").parsesTo("message", "hello");
    f.assertThat("-m", "hello").parsesTo("message", "hello");
    f.assertThat("--message=hello").parsesTo("message", "hello");
    f.assertThat("--message", "hello").parsesTo("message", "hello");
  }

  @Test
  public void testRepeatableShortAttached() {
    f.assertThat("-fbar.txt").parsesTo(
        "file", singletonList("bar.txt"));
    f.assertThat("-fbar.txt", "--message=hello").parsesTo(
        "message", "hello",
        "file", singletonList("bar.txt"));
    f.assertThat("--message=hello", "-fbar.txt").parsesTo(
        "message", "hello",
        "file", singletonList("bar.txt"));
  }

  @Test
  public void testLongSuppressed() {
    // Long option --cmos is suppressed
    f.assertThat("--cmos").failsWithLine1("Invalid option: --cmos");
  }

  @Test
  public void testFlag() {
    f.assertThat("-c", "hello").parsesTo(
        "cmos", true,
        "otherTokens", singletonList("hello"));
  }

  @Test
  public void testPositionalOnly() {
    f.assertThat("hello", "goodbye").parsesTo(
        "otherTokens", asList("hello", "goodbye"));
  }

  @Test
  public void twoFlags() {
    f.assertThat("-c", "-v").parsesTo(
        "cmos", true,
        "verbose", true);
  }

  @Test
  public void errorSuspiciousInput() {
    f.assertThat("-cvm", "hello").failsWithLine1("Invalid option: -cvm");
  }


  @Test
  public void testPrint() {
    f.assertPrints(
        "SYNOPSIS",
        "  [OPTION]... [OTHER_TOKENS]... [-- DD_TOKENS...]",
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
