package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

class NoNameArgumentsTest {

  private final ParserTestFixture<NoNameArguments> f =
      ParserTestFixture.create(new NoNameArgumentsParser());

  @Test
  void testDifferentOrder() {
    Object[] expected = {
        "message", Optional.of("m"),
        "file", asList("f", "o", "o"),
        "verbosity", Optional.empty(),
        "number", 1,
        "cmos", true};
    f.assertThat("--message=m", "--file=f", "--file=o", "--file=o", "--cmos", "-n1")
        .succeeds(expected);
    f.assertThat("-n1", "--cmos", "--message=m", "--file=f", "--file=o", "--file=o")
        .succeeds(expected);
    f.assertThat("--file", "f", "--message=m", "--file", "o", "--cmos", "-n1", "--file", "o")
        .succeeds(expected);
  }

  @Test
  void testFlag() {
    f.assertThat("--cmos", "-n1").succeeds(
        "message", Optional.empty(),
        "file", emptyList(),
        "verbosity", Optional.empty(),
        "number", 1,
        "cmos", true);
  }

  @Test
  void testOptionalInt() {
    f.assertThat("-v", "1", "-n1").succeeds(
        "message", Optional.empty(),
        "file", emptyList(),
        "verbosity", Optional.of(1),
        "number", 1,
        "cmos", false);
    f.assertThat("-n1").succeeds(
        "message", Optional.empty(),
        "file", emptyList(),
        "verbosity", Optional.empty(),
        "number", 1,
        "cmos", false);
  }

  @Test
  void errorMissingInt() {
    f.assertThat("--cmos").failsWithMessage("Missing required option: NUMBER (-n, --number)");
  }

  @Test
  void errorUnknownToken() {
    f.assertThat("blabla").failsWithMessage("Excess param: blabla");
  }

  @Test
  void testPrint() {
    f.assertPrintsHelp(
        "USAGE",
        "  no-name-arguments [OPTION]... -n NUMBER",
        "",
        "OPTIONS",
        "  --message MESSAGE         ",
        "  --file FILE               ",
        "  -v, --verbosity VERBOSITY ",
        "  -n, --number NUMBER       ",
        "  --cmos                    ",
        "");
  }

}
