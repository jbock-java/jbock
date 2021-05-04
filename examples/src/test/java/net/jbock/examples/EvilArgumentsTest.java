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
        "  evil-arguments --fancy FANCY --fAncy FANCY --f_ancy F_ANCY --f__ancy F__ANCY",
        "        --blub BLUB --Blub BLUB",
        "",
        "OPTIONS",
        "  --fancy FANCY     ",
        "  --fAncy FANCY     ",
        "  --f_ancy F_ANCY   ",
        "  --f__ancy F__ANCY ",
        "  --blub BLUB       ",
        "  --Blub BLUB       ",
        "");
  }
}
