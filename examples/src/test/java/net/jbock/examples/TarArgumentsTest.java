package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

class TarArgumentsTest {

  private ParserTestFixture<TarArguments> f =
      ParserTestFixture.create(TarArguments_Parser.create());

  @Test
  void testExtract() {
    f.assertThat("-x", "-f", "foo.tar").succeeds(
        "extract", true,
        "file", "foo.tar",
        "create", false,
        "verbose", false,
        "compress", false);
    f.assertThat("-v", "-x", "-f", "foo.tar").succeeds(
        "extract", true,
        "file", "foo.tar",
        "create", false,
        "verbose", true,
        "compress", false);
  }

  @Test
  void noGrouping() {
    f.assertThat("-v", "xf", "foo.tar").failsWithLine1("Invalid option: xf");
    f.assertThat("-v", "-xf", "foo.tar").failsWithLine1("Invalid option: -xf");
  }

  @Test
  void testPrint() {
    f.assertPrints(
        "NAME",
        "  TarArguments",
        "",
        "SYNOPSIS",
        "  TarArguments [OPTION]... -f FILE",
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
