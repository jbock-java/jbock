package net.jbock.examples;

import net.jbock.examples.fixture.ParserFixture;
import org.junit.jupiter.api.Test;

public class CpArgumentsTest {

  private final ParserFixture<CpArguments> f =
      ParserFixture.create(CpArguments_Parser::parse);

  @Test
  public void errorMissingSource() {
    f.assertThat().failsWithLine1("Missing parameter: SOURCE");
    f.assertThat("-r").failsWithLine1("Missing parameter: SOURCE");
  }

  @Test
  public void errorMissingDest() {
    f.assertThat("a").failsWithLine1("Missing parameter: DEST");
    f.assertThat("a", "-r").failsWithLine1("Missing parameter: DEST");
    f.assertThat("-r", "a").failsWithLine1("Missing parameter: DEST");
  }

  @Test
  public void minimal() {
    f.assertThat("a", "b").succeeds(
        "source", "a",
        "dest", "b");
    f.assertThat("b", "a").succeeds(
        "source", "b",
        "dest", "a");
  }

  @Test
  public void dashNotIgnored() {
    f.assertThat("-a", "b").failsWithLine1("Invalid option: -a");
  }

  @Test
  public void tooMany() {
    f.assertThat("a", "b", "c").failsWithLine1("Invalid option: c");
  }

  @Test
  public void tooManyAndFlag() {
    f.assertThat("-r", "a", "b", "c").failsWithLine1("Invalid option: c");
  }

  @Test
  public void flagInVariousPositions() {
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
  public void testPrint() {
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
