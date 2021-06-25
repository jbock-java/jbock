package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

class NoNameArgumentsTest {

  private final NoNameArgumentsParser parser = new NoNameArgumentsParser();

  private final ParserTestFixture<NoNameArguments> f =
      ParserTestFixture.create(parser::parse);

  @Test
  void testDifferentOrder() {
    Object[] expected = {
        "message", Optional.of("m"),
        "file", List.of("f", "o", "o"),
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
        "file", List.of(),
        "verbosity", Optional.empty(),
        "number", 1,
        "cmos", true);
  }

  @Test
  void testOptionalInt() {
    f.assertThat("-v", "1", "-n1").succeeds(
        "message", Optional.empty(),
        "file", List.of(),
        "verbosity", Optional.of(1),
        "number", 1,
        "cmos", false);
    f.assertThat("-n1").succeeds(
        "message", Optional.empty(),
        "file", List.of(),
        "verbosity", Optional.empty(),
        "number", 1,
        "cmos", false);
  }

  @Test
  void errorMissingInt() {
    f.assertThat("--cmos").fails("Missing required option NUMBER (-n, --number)");
  }

  @Test
  void errorUnknownToken() {
    f.assertThat("blabla").fails("Excess param: blabla");
  }

  @Test
  void testPrint() {
    f.assertPrintsHelp(
        "\u001B[1mUSAGE\u001B[m",
        "  no-name-arguments [OPTIONS] -n NUMBER",
        "",
        "\u001B[1mOPTIONS\u001B[m",
        "  --message MESSAGE         ",
        "  --file FILE               ",
        "  -v, --verbosity VERBOSITY ",
        "  -n, --number NUMBER       ",
        "  --cmos                    ",
        "");
  }

}
