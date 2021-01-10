package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;

class RmArgumentsTest {

  private final ParserTestFixture<RmArguments> f =
      ParserTestFixture.create(new RmArguments_Parser());

  @Test
  void testRest() {
    f.assertThat("-f", "a", "--", "-r", "--", "-f").succeeds(
        "recursive", false,
        "force", true,
        "otherTokens", asList("a", "-r", "--", "-f"));
  }

  @Test
  void testPrint() {
    f.assertPrintsHelp(
        "Usage: rm-arguments [options...] <other_tokens>...",
        "  other_tokens     This is a list that may be empty.",
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
