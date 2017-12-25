package net.jbock.examples;

import net.jbock.examples.fixture.ParserFixture;
import org.junit.Test;

public class HelplessArgumentsTest {

  private final ParserFixture<HelplessArguments> f =
      ParserFixture.create(HelplessArguments_Parser::parse);

  @Test
  public void errorNoArguments() throws Exception {
    f.assertThat().failsWithLines(
        "NAME",
        "  HelplessArguments",
        "",
        "SYNOPSIS",
        "  HelplessArguments [OPTION]... REQUIRED",
        "",
        "DESCRIPTION",
        "",
        "  --help",
        "",
        "Missing parameter: REQUIRED",
        ""
    );
  }
}
