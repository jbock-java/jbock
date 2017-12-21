package net.jbock.examples;

import static net.jbock.examples.fixture.PrintFixture.printFixture;

import net.jbock.examples.fixture.ParserFixture;
import org.junit.Test;

public class CpArgumentsTest {

  private final ParserFixture<CpArguments> f =
      ParserFixture.create(CpArguments_Parser::parse);

  @Test
  public void errorMissingSource() {
    f.assertThat().isInvalid("Missing positional parameter: SOURCE");
    f.assertThat("-r").isInvalid("Missing positional parameter: SOURCE");
  }

  @Test
  public void errorMissingDest() {
    f.assertThat("a").isInvalid("Missing positional parameter: DEST");
    f.assertThat("a", "-r").isInvalid("Missing positional parameter: DEST");
    f.assertThat("-r", "a").isInvalid("Missing positional parameter: DEST");
  }

  @Test
  public void minimal() {
    f.assertThat("a", "b").isParsedAs(
        "source", "a",
        "dest", "b");
    f.assertThat("b", "a").isParsedAs(
        "source", "b",
        "dest", "a");
  }

  @Test
  public void dashNotIgnored() {
    f.assertThat("-a", "b").isInvalid("Invalid option: -a");
  }

  @Test
  public void tooMany() {
    f.assertThat("a", "b", "c").isInvalid("Excess option: c");
  }

  @Test
  public void tooManyAndFlag() {
    f.assertThat("-r", "a", "b", "c").isInvalid("Excess option: c");
  }

  @Test
  public void flagInVariousPositions() {
    Object[] expected = new Object[]{
        "source", "a",
        "dest", "b",
        "recursive", true};
    f.assertThat("-r", "a", "b")
        .isParsedAs(expected);
    f.assertThat("a", "-r", "b")
        .isParsedAs(expected);
    f.assertThat("a", "b", "-r")
        .isParsedAs(expected);
  }

  @Test
  public void testPrint() {
    printFixture(CpArguments_Parser::printUsage).assertPrints(
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
