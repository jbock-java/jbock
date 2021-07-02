package net.jbock.examples;

import net.jbock.either.Optional;
import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

class EvilArgumentsTest {

  private final EvilArgumentsParser parser = new EvilArgumentsParser();

  private final ParserTestFixture<EvilArguments> f =
      ParserTestFixture.create(parser::parse);

  @Test
  void basicTest() {
    f.assertThat("--Fancy=1", "--fancy=1", "--fAncy=2", "--f_ancy=3",
        "--f__ancy=3", "--blub=4", "--Blub=5")
        .has(EvilArguments::Fancy, Optional.of("1"))
        .has(EvilArguments::fancy, "1")
        .has(EvilArguments::fAncy, "2")
        .has(EvilArguments::f_ancy, "3")
        .has(EvilArguments::f__ancy, "3")
        .has(EvilArguments::blub, "4")
        .has(EvilArguments::Blub, "5");
  }

  @Test
  void testPrint() {
    f.assertPrintsHelp(
        "\u001B[1mUSAGE\u001B[m",
        "  evil-arguments [OPTIONS] --fancy FANCY --fAncy FANCY --f_ancy F_ANCY",
        "        --f__ancy F__ANCY --blub BLUB --Blub BLUB",
        "",
        "\u001B[1mOPTIONS\u001B[m",
        "  --Fancy FANCY     ",
        "  --fancy FANCY     ",
        "  --fAncy FANCY     ",
        "  --f_ancy F_ANCY   ",
        "  --f__ancy F__ANCY ",
        "  --blub BLUB       ",
        "  --Blub BLUB       ",
        "  --evil EVIL       ",
        "");
  }
}
