package net.jbock.examples;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

class RmArgumentsTest {

  private ParserTestFixture<RmArguments> f =
      ParserTestFixture.create(RmArguments_Parser.newBuilder());

  @Test
  void testRest() {
    f.assertThat("-f", "a", "--", "-r", "--", "-f").succeeds(
        "force", true,
        "recursive", false,
        "otherTokens", singletonList("a"),
        "ddTokens", asList("-r", "--", "-f"));
  }

  @Test
  void testPrint() {
    f.assertPrints(
        "NAME",
        "  RmArguments",
        "",
        "SYNOPSIS",
        "  RmArguments [OPTION]... [OTHER_TOKENS]... [-- DD_TOKENS...]",
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
