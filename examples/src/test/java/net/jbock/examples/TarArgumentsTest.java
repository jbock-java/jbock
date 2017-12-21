package net.jbock.examples;

import static net.jbock.examples.fixture.PrintFixture.printFixture;

import net.jbock.examples.fixture.ParserFixture;
import org.junit.Test;

public class TarArgumentsTest {

  private final ParserFixture<TarArguments> f =
      ParserFixture.create(TarArguments_Parser::parse);

  @Test
  public void testExtract() {
    f.assertThat("-x", "-f", "foo.tar").isParsedAs(
        "extract", true,
        "file", "foo.tar");
    f.assertThat("-v", "-x", "-f", "foo.tar").isParsedAs(
        "extract", true,
        "file", "foo.tar",
        "verbose", true);
  }

  @Test
  public void noGrouping() {
    f.assertThat("-v", "xf", "foo.tar").isInvalid("Invalid option: xf");
    f.assertThat("-v", "-xf", "foo.tar").isInvalid("Invalid option: -xf");
  }

  @Test
  public void testPrint() {
    printFixture(TarArguments_Parser::printUsage).assertPrints(
        "SYNOPSIS",
        "  [OPTION]... -f FILE",
        "",
        "DESCRIPTION",
        "",
        "  -x, --extract",
        "",
        "  -c, --create",
        "",
        "  -v, --verbose",
        "",
        "  -z, --compress",
        "",
        "  -f, --file VALUE",
        "",
        "");
  }
}
