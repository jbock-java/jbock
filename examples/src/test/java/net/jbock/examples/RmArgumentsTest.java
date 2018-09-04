package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;

class RmArgumentsTest {

  private ParserTestFixture<RmArguments> f =
      ParserTestFixture.create(RmArguments_Parser.create());

  @Test
  void testRest() {
    f.assertThat("-f", "a", "--", "-r", "--", "-f").succeeds(
        "force", true,
        "recursive", false,
        "otherTokens", asList("a", "-r", "--", "-f"));
  }

  @Test
  void testPrint() {
    f.assertPrints(
        "NAME",
        "  RmArguments",
        "",
        "SYNOPSIS",
        "  RmArguments [<options>] [[--] <other_tokens...>]",
        "",
        "DESCRIPTION",
        "",
        "  -r, --recursive",
        "",
        "  -f, --force",
        "",
        "");
  }
}
