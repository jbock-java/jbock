package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

class EvilArgumentsTest {

  private ParserTestFixture<EvilArguments> f =
      ParserTestFixture.create(EvilArguments_Parser.create());

  @Test
  void basicTest() {
    f.assertThat("--fancy=1", "--fAncy=2", "--f_ancy=3", "--f__ancy=3", "--blub=4", "--Blub=5").succeeds(
        "fancy", "1",
        "fAncy", "2",
        "f_ancy", "3",
        "f__ancy", "3",
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
        "  EvilArguments --fancy=<FANCY> --fAncy=<FANCY_1> --f_ancy=<F_ANCY> --f__ancy=<F__ANCY> --blub=<BLUB> --Blub=<BLUB_5>",
        "",
        "DESCRIPTION",
        "",
        "OPTIONS",
        "  --fancy <FANCY>",
        "",
        "  --fAncy <FANCY_1>",
        "",
        "  --f_ancy <F_ANCY>",
        "",
        "  --f__ancy <F__ANCY>",
        "",
        "  --blub <BLUB>",
        "",
        "  --Blub <BLUB_5>",
        "",
        "  --help",
        "    Print this help page.",
        "    The help flag may only be passed as the first argument.",
        "    Any further arguments will be ignored.",
        "",
        "");
  }
}
