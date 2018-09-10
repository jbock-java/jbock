package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

class CpArgumentsTest {

  private ParserTestFixture<CpArguments> f =
      ParserTestFixture.create(CpArguments_Parser.create());

  @Test
  void errorMissingSource() {
    f.assertThat().failsWithLine4("Missing parameter: <SOURCE>");
    f.assertThat("-r").failsWithLine4("Missing parameter: <SOURCE>");
  }

  @Test
  void errorMissingDest() {
    f.assertThat("a").failsWithLine4("Missing parameter: <DEST>");
    f.assertThat("a", "-r").failsWithLine4("Missing parameter: <DEST>");
    f.assertThat("-r", "a").failsWithLine4("Missing parameter: <DEST>");
  }

  @Test
  void minimal() {
    f.assertThat("a", "b").succeeds(
        "recursive", false,
        "source", "a",
        "dest", "b");
    f.assertThat("b", "a").succeeds(
        "recursive", false,
        "source", "b",
        "dest", "a");
  }

  @Test
  void dashNotIgnored() {
    f.assertThat("-a", "b").failsWithLine4("Invalid option: -a");
  }

  @Test
  void tooMany() {
    f.assertThat("a", "b", "c").failsWithLine4("Invalid option: c");
  }

  @Test
  void tooManyAndFlag() {
    f.assertThat("-r", "a", "b", "c").failsWithLine4("Invalid option: c");
  }

  @Test
  void flagInVariousPositions() {
    Object[] expected = new Object[]{
        "recursive", true,
        "source", "a",
        "dest", "b"};
    f.assertThat("-r", "a", "b")
        .succeeds(expected);
    f.assertThat("a", "-r", "b")
        .succeeds(expected);
    f.assertThat("a", "b", "-r")
        .succeeds(expected);
  }

  @Test
  void testPrint() {
    f.assertPrints(
        "NAME",
        "  CpArguments",
        "",
        "SYNOPSIS",
        "  CpArguments [<options>] <SOURCE> <DEST>",
        "",
        "DESCRIPTION",
        "",
        "SOURCE",
        "",
        "DEST",
        "",
        "OPTIONS",
        "  -r, --recursive",
        "",
        "  --help",
        "    Print this help page.",
        "    The help flag may only be passed as the first argument.",
        "    Any further arguments will be ignored.",
        "",
        "");
  }
}
