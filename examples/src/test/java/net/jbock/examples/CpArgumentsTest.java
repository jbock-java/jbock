package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

class CpArgumentsTest {

  private ParserTestFixture<CpArguments> f =
      ParserTestFixture.create(CpArguments_Parser.newBuilder());

  @Test
  void errorMissingSource() {
    f.assertThat().failsWithLine1("Missing parameter: SOURCE");
    f.assertThat("-r").failsWithLine1("Missing parameter: SOURCE");
  }

  @Test
  void errorMissingDest() {
    f.assertThat("a").failsWithLine1("Missing parameter: DEST");
    f.assertThat("a", "-r").failsWithLine1("Missing parameter: DEST");
    f.assertThat("-r", "a").failsWithLine1("Missing parameter: DEST");
  }

  @Test
  void minimal() {
    f.assertThat("a", "b").succeeds(
        "source", "a",
        "dest", "b",
        "recursive", false);
    f.assertThat("b", "a").succeeds(
        "source", "b",
        "dest", "a",
        "recursive", false);
  }

  @Test
  void dashNotIgnored() {
    f.assertThat("-a", "b").failsWithLine1("Invalid option: -a");
  }

  @Test
  void tooMany() {
    f.assertThat("a", "b", "c").failsWithLine1("Invalid option: c");
  }

  @Test
  void tooManyAndFlag() {
    f.assertThat("-r", "a", "b", "c").failsWithLine1("Invalid option: c");
  }

  @Test
  void flagInVariousPositions() {
    Object[] expected = new Object[]{
        "source", "a",
        "dest", "b",
        "recursive", true};
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
        "  CpArguments [OPTION]... SOURCE DEST",
        "",
        "DESCRIPTION",
        "",
        "  -r, --recursive",
        "",
        "");
  }
}
