package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

class NoNameArgumentsTest {

  private ParserTestFixture<NoNameArguments> f =
      ParserTestFixture.create(NoNameArguments_Parser.newBuilder());

  @Test
  void testDifferentOrder() {
    Object[] expected = {
        "message", "m",
        "cmos", true,
        "number", 1,
        "file", asList("f", "o", "o"),
        "verbosity", null};
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
        "file", emptyList(),
        "number", 1,
        "message", null,
        "cmos", true,
        "verbosity", null);
  }

  @Test
  void testOptionalInt() {
    f.assertThat("-v", "1", "-n1").succeeds(
        "cmos", false,
        "message", null,
        "file", emptyList(),
        "verbosity", 1,
        "number", 1);
    f.assertThat("-n1").succeeds(
        "cmos", false,
        "message", null,
        "file", emptyList(),
        "number", 1,
        "verbosity", null);
  }

  @Test
  void errorMissingInt() {
    f.assertThat("--cmos").failsWithLine1("Missing required option: NUMBER (-n, --number)");
  }

  @Test
  void errorUnknownToken() {
    f.assertThat("blabla").failsWithLine1("Invalid option: blabla");
  }


  @Test
  void testPrint() {
    f.assertPrints(
        "NAME",
        "  NoNameArguments",
        "",
        "SYNOPSIS",
        "  NoNameArguments [OPTION]... -n NUMBER",
        "",
        "DESCRIPTION",
        "",
        "  --message VALUE",
        "",
        "  --file VALUE...",
        "",
        "  -v, --verbosity NUMBER",
        "",
        "  -n, --number NUMBER",
        "",
        "  --cmos",
        "",
        "");
  }

}
