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
        "recursive", false,
        "force", true,
        "otherTokens", asList("a", "-r", "--", "-f"));
  }

  @Test
  void testPrint() {
    f.assertPrintsHelp(
        "NAME",
        "  RmArguments",
        "",
        "SYNOPSIS",
        "  RmArguments [OPTIONS...] [<other_tokens>...]",
        "",
        "DESCRIPTION",
        "",
        "OTHER_TOKENS",
        "",
        "OPTIONS",
        "  -r",
        "",
        "  -f",
        "",
        "  --help",
        "    print online help",
        "",
        "");
  }
}
