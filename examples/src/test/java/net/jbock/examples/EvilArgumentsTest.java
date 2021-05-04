package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

class EvilArgumentsTest {

  private final ParserTestFixture<EvilArguments> f =
      ParserTestFixture.create(new EvilArguments_Parser());

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
    f.assertPrintsHelp(
        "USAGE",
        "  evil-arguments --fancy FANCY --fAncy F_ANCY --f_ancy F_ANCY_2 --f__ancy F_ANCY_3",
        "        --blub BLUB --Blub BLUB_5",
        "",
        "OPTIONS",
        "  --fancy FANCY      ",
        "  --fAncy F_ANCY     ",
        "  --f_ancy F_ANCY_2  ",
        "  --f__ancy F_ANCY_3 ",
        "  --blub BLUB        ",
        "  --Blub BLUB_5      ",
        "");
  }
}
