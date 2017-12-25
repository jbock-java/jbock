package net.jbock.examples;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import net.jbock.examples.fixture.ParserFixture;
import org.junit.Test;

public class RmArgumentsTest {

  private final ParserFixture<RmArguments> f =
      ParserFixture.create(RmArguments_Parser::parse);

  @Test
  public void testRest() {
    f.assertThat("-f", "a", "--", "-r", "--", "-f").parsesTo(
        "force", true,
        "otherTokens", singletonList("a"),
        "ddTokens", asList("-r", "--", "-f"));
  }

  @Test
  public void testPrint() {
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
