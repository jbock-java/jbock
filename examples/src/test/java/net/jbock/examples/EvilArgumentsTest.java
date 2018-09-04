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
        "  EvilArguments --fancy=<FANCY> --fAncy=<FANCY_1> --f_ancy=<F_ANCY> --blub=<BLUB> --Blub=<BLUB_4>",
        "",
        "DESCRIPTION",
        "",
        "  --fancy <FANCY>",
        "",
        "  --fAncy <FANCY_1>",
        "",
        "  --f_ancy <F_ANCY>",
        "",
        "  --blub <BLUB>",
        "",
        "  --Blub <BLUB_4>",
        "",
        "");
  }
}
