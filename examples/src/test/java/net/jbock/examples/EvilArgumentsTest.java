package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

class EvilArgumentsTest {

  private ParserTestFixture<EvilArguments> f =
      ParserTestFixture.create(EvilArguments_Parser.create());

  @Test
  void basicTest() {
    f.assertThat("--fancy=1", "--fAncy=2", "--f_ancy=3", "--blub=4", "--Blub=5").succeeds(
        "fancy", "1",
        "fAncy", "2",
        "f_ancy", "3",
        "blub", "4",
        "Blub", "5");
  }

  @Test
  void testPrint() {
    f.assertPrints(
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
