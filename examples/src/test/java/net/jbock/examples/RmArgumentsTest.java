package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static net.jbock.examples.fixture.ParserTestFixture.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RmArgumentsTest {

  private final RmArgumentsParser parser = new RmArgumentsParser();

  private final ParserTestFixture<RmArguments> f =
      ParserTestFixture.create(parser::parse);

  @Test
  void testRest() {
    f.assertThat("-f", "a", "--", "-r", "--", "-f").succeeds(
        "recursive", false,
        "force", true,
        "otherTokens", asList("a", "-r", "--", "-f"));
  }

  @Test
  void testInvalidToken() {
    assertTrue(parser.parse("--foo-bar").getLeft().map(f::castToError)
        .orElseThrow().message()
        .contains("Invalid option: --foo-bar"));
  }

  @Test
  void testPrint() {
    String[] actual = parser.parse("--help")
        .getLeft().map(f::getUsageDocumentation).orElseThrow();
    assertEquals(actual,
        "\u001B[1mUSAGE\u001B[m",
        "  rm-arguments [OPTIONS] OTHER_TOKENS...",
        "",
        "\u001B[1mPARAMETERS\u001B[m",
        "  OTHER_TOKENS  This is a list that may be empty.",
        "",
        "\u001B[1mOPTIONS\u001B[m",
        "  -r, --recursive  ALLES TURISTEN UND NONTEKNISCHEN LOOKENSPEEPERS! DAS",
        "                   KOMPUTERMASCHINE IST NICHT FUR DER GEFINGERPOKEN UND",
        "                   MITTENGRABEN! ODERWISE IST EASY TO SCHNAPPEN DER SPRINGENWERK,",
        "                   BLOWENFUSEN UND POPPENCORKEN MIT SPITZENSPARKEN. IST NICHT FUR",
        "                   GEWERKEN BEI DUMMKOPFEN. DER RUBBERNECKEN SIGHTSEEREN KEEPEN",
        "                   DAS COTTONPICKEN HANDER IN DAS POCKETS MUSS. ZO RELAXEN UND",
        "                   WATSCHEN DER BLINKENLICHTEN.",
        "  -f, --force      Use the force, Luke.",
        "");
  }
}
