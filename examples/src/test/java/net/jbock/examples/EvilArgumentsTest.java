package net.jbock.examples;

import net.jbock.examples.fixture.ParserFixture;
import org.junit.Test;

public class EvilArgumentsTest {

  private final ParserFixture<EvilArguments> f =
      ParserFixture.create(EvilArguments_Parser::parse);

  @Test
  public void basicTest() {
    f.assertThat("--fancy=1", "--fAncy=2", "--f_ancy=3", "--blub=4", "--Blub=5").parsesTo(
        "fancy", "1",
        "fAncy", "2",
        "f_ancy", "3",
        "blub", "4",
        "Blub", "5");
  }

  @Test
  public void testPrint() {
    ParserFixture.create(EvilArguments_Parser::parse).assertPrints(
        "NAME",
        "  EvilArguments",
        "",
        "SYNOPSIS",
        "  EvilArguments --fancy=FANCY_0 --fAncy=F_ANCY_1 --f_ancy=F_ANCY_2 --blub=BLUB_3 --Blub=BLUB_4",
        "",
        "DESCRIPTION",
        "",
        "  --fancy VALUE",
        "",
        "  --fAncy VALUE",
        "",
        "  --f_ancy VALUE",
        "",
        "  --blub VALUE",
        "",
        "  --Blub VALUE",
        "",
        "");
  }
}
