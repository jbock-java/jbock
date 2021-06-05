package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;

class RmArgumentsTest {

  private final ParserTestFixture<RmArguments> f =
      ParserTestFixture.create(new RmArgumentsParser());

  @Test
  void testRest() {
    f.assertThat("-f", "a", "--", "-r", "--", "-f").succeeds(
        "recursive", false,
        "force", true,
        "otherTokens", asList("a", "-r", "--", "-f"));
  }

  @Test
  void testInvalidToken() {
    f.assertThat("--foo-bar").failsWithMessage("Invalid option: --foo-bar");
  }

  @Test
  void testPrint() {
    f.assertPrintsHelp(
        "\u001B[1mUSAGE\u001B[m",
        "  rm-arguments [OPTION]... [OTHER_TOKENS]...",
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
