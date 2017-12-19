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
    f.assertThat("-m", "hello", "--message=goodbye").isInvalid(
        "Option MESSAGE (-m, --message) is not repeatable");
  }

  @Test
  public void errorMissingValue() {
    // there's nothing after -m
    f.assertThat("-m").isInvalid("Missing value after token: -m");
  }

  @Test
  public void errorLongShortConflict() {
    f.assertThat("--message=hello", "-m", "goodbye").isInvalid(
        "Option MESSAGE (-m, --message) is not repeatable");
  }

  @Test
  public void errorLongLongConflict() {
    f.assertThat("--message=hello", "--message=goodbye").isInvalid(
        "Option MESSAGE (-m, --message) is not repeatable");
  }

  @Test
  public void errorInvalidOption() {
    f.assertThat("-c1").isInvalid("Invalid option: -c1");
    f.assertThat("-c-v").isInvalid("Invalid option: -c-v");
    f.assertThat("-c-").isInvalid("Invalid option: -c-");
    f.assertThat("-c=v").isInvalid("Invalid option: -c=v");
    f.assertThat("-c=").isInvalid("Invalid option: -c=");
    f.assertThat("-cX=1").isInvalid("Invalid option: -cX=1");
    f.assertThat("-cvv").isInvalid("Invalid option: -cvv");
    f.assertThat("-cvx").isInvalid("Invalid option: -cvx");
    f.assertThat("-cvm").isInvalid("Invalid option: -cvm");
  }

  @Test
  public void testDetachedLong() {
    f.assertThat("--message", "hello").isParsedAs(
        "message", "hello");
  }

  @Test
  public void testInterestingTokens() {
    f.assertThat("--message=hello", "b-a-b-a", "--", "->", "<=>", "", " ").isParsedAs(
        "message", "hello",
        "otherTokens", singletonList("b-a-b-a"),
        "ddTokens", asList("->", "<=>", "", " "));
  }

  @Test
  public void testPassEmptyString() {
    f.assertThat("-m", "").isParsedAs("message", "");
    f.assertThat("--message=").isParsedAs("message", "");
    f.assertThat("--message", "").isParsedAs("message", "");
  }

  @Test
  public void testAllForms() {
    f.assertThat("-mhello").isParsedAs("message", "hello");
    f.assertThat("-m", "hello").isParsedAs("message", "hello");
    f.assertThat("--message=hello").isParsedAs("message", "hello");
    f.assertThat("--message", "hello").isParsedAs("message", "hello");
  }

  @Test
  public void testRepeatableShortAttached() {
    f.assertThat("-fbar.txt").isParsedAs(
        "file", singletonList("bar.txt"));
    f.assertThat("-fbar.txt", "--message=hello").isParsedAs(
        "message", "hello",
        "file", singletonList("bar.txt"));
    f.assertThat("--message=hello", "-fbar.txt").isParsedAs(
        "message", "hello",
        "file", singletonList("bar.txt"));
  }

  @Test
  public void testLongSuppressed() {
    // Long option --cmos is suppressed
    f.assertThat("--cmos").isInvalid("Invalid option: --cmos");
  }

  @Test
  public void testFlag() {
    f.assertThat("-c", "hello").isParsedAs(
        "cmos", true,
        "otherTokens", singletonList("hello"));
  }

  @Test
  public void testPositionalOnly() {
    f.assertThat("hello", "goodbye").isParsedAs(
        "otherTokens", asList("hello", "goodbye"));
  }

  @Test
  public void twoFlags() {
    f.assertThat("-c", "-v").isParsedAs(
        "cmos", true,
        "verbose", true);
  }

  @Test
  public void errorSuspiciousInput() {
    f.assertThat("-cvm", "hello").isInvalid("Invalid option: -cvm");
  }
}
