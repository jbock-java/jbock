package net.jbock.examples;

import net.jbock.examples.fixture.ParserFixture;
import org.junit.Test;

public class CpArgumentsTest {

  private final ParserFixture<CpArguments> f =
      ParserFixture.create(CpArguments_Parser::parse);

  @Test
  public void errorMissingSource() {
    f.assertThat().fails("Missing parameter: SOURCE");
    f.assertThat("-r").fails("Missing parameter: SOURCE");
  }

  @Test
  public void errorMissingDest() {
    f.assertThat("a").fails("Missing parameter: DEST");
    f.assertThat("a", "-r").fails("Missing parameter: DEST");
    f.assertThat("-r", "a").fails("Missing parameter: DEST");
  }

  @Test
  public void minimal() {
    f.assertThat("a", "b").parsesTo(
        "source", "a",
        "dest", "b");
    f.assertThat("b", "a").parsesTo(
        "source", "b",
        "dest", "a");
  }

  @Test
  public void dashNotIgnored() {
    f.assertThat("-a", "b").fails("Invalid option: -a");
  }

  @Test
  public void tooMany() {
    f.assertThat("a", "b", "c").fails("Invalid option: c");
  }

  @Test
  public void tooManyAndFlag() {
    f.assertThat("-r", "a", "b", "c").fails("Invalid option: c");
  }

  @Test
  public void flagInVariousPositions() {
    Object[] expected = new Object[]{
        "source", "a",
        "dest", "b",
        "recursive", true};
    f.assertThat("-r", "a", "b")
        .parsesTo(expected);
    f.assertThat("a", "-r", "b")
        .parsesTo(expected);
    f.assertThat("a", "b", "-r")
        .parsesTo(expected);
  }

  @Test
  public void testPrint() {
    f.assertPrints(
        "SYNOPSIS",
        "  [OPTION]... SOURCE DEST",
        "",
        "DESCRIPTION",
        "",
        "  -r, --recursive",
        "",
        "");
  }
}
