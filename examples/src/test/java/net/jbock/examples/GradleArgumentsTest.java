package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GradleArgumentsTest {

  private final GradleArgumentsParser parser = new GradleArgumentsParser();

  private final ParserTestFixture<GradleArguments> f =
      ParserTestFixture.create(parser::parse);

  @Test
  void errorShortLongConflict() {
    f.assertThat("-m", "hello", "--message=goodbye")
        .fails("Option '--message=goodbye' is a repetition");
  }

  @Test
  void errorMissingValue() {
    f.assertThat("-m").fails("Missing argument after token: -m");
  }

  @Test
  void errorLongShortConflict() {
    f.assertThat("--message=hello", "-m", "goodbye")
        .fails("Option '-m' is a repetition");
  }

  @Test
  void errorLongLongConflict() {
    f.assertThat("--message=hello", "--message=goodbye")
        .fails("Option '--message=goodbye' is a repetition");
  }

  @Test
  void errorInvalidOption() {
    f.assertThat("-c1").fails("Invalid token: -c1");
    f.assertThat("-c-v").fails("Invalid token: -c-v");
    f.assertThat("-c-").fails("Invalid token: -c-");
    f.assertThat("-c=v").fails("Invalid token: -c=v");
    f.assertThat("-c=").fails("Invalid token: -c=");
    f.assertThat("-cX=1").fails("Invalid token: -cX=1");
    f.assertThat("-cvv").fails("Option '-v' is a repetition");
    f.assertThat("-cvx").fails("Invalid token: -cvx");
    f.assertThat("-cvm").fails("Missing argument after token: -m");
    f.assertThat("--column-count").fails("Invalid option: --column-count");
    f.assertThat("--cmos").fails("Invalid option: --cmos");
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
        "mainToken", Optional.of("b-a-b-a"),
        "otherTokens", asList("->", "<=>", "", " "));
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
  void testFlag() {
    f.assertThat("-c", "hello", "hello").succeeds(
        "message", Optional.empty(),
        "file", emptyList(),
        "dir", Optional.empty(),
        "cmos", true,
        "verbose", false,
        "mainToken", Optional.of("hello"),
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
        "mainToken", Optional.of("hello"),
        "otherTokens", singletonList("goodbye"));
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
  void testClustering() {
    f.assertThat("-cv").succeeds(
        "message", Optional.empty(),
        "file", emptyList(),
        "dir", Optional.empty(),
        "cmos", true,
        "verbose", true,
        "otherTokens", emptyList());
    f.assertThat("-cvm", "hello").succeeds(
        "message", Optional.of("hello"),
        "file", emptyList(),
        "dir", Optional.empty(),
        "cmos", true,
        "verbose", true,
        "otherTokens", emptyList());
    f.assertThat("-cvmhello").succeeds(
        "message", Optional.of("hello"),
        "file", emptyList(),
        "dir", Optional.empty(),
        "cmos", true,
        "verbose", true,
        "otherTokens", emptyList());
  }

  @Test
  void testPrint() {
    f.assertPrintsHelp(
        "\u001B[1mUSAGE\u001B[m",
        "  gradle-arguments [OPTIONS] [SOME_TOKEN] moreTokens...",
        "",
        "\u001B[1mPARAMETERS\u001B[m",
        "  SOME_TOKEN  some token",
        "  moreTokens  some more tokens",
        "",
        "\u001B[1mOPTIONS\u001B[m",
        "  -m, --message MESSAGE  the message message goes here",
        "  -f, --file INPUT_FILE  the files",
        "  --dir INPUT_DIR        the dir",
        "  -c, --c                cmos flag",
        "  -v, --verbose         ",
        "");
  }
}
