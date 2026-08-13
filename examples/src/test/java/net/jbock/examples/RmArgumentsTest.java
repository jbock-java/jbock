package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.List;

class RmArgumentsTest {

    private final ParserTestFixture<RmArguments> f =
            ParserTestFixture.create(RmArgumentsParser::parse);

    @Test
    void testRest() {
        f.assertThat("-f", "a", "--", "-r", "--", "-f")
                .has(RmArguments::recursive, false)
                .has(RmArguments::force, true)
                .has(RmArguments::otherTokens, List.of("a", "-r", "--", "-f"));
    }

    @Test
    void testInvalidToken() {
        f.assertThat("--foo-bar").fails("Invalid option: --foo-bar");
    }

    @Test
    void testPrint() {
        f.assertPrintsHelp(
                RmArgumentsParser.createModel(),
                "USAGE",
                "  rm-arguments [OPTIONS] OTHER_TOKENS...",
                "",
                "PARAMETERS",
                "  OTHER_TOKENS  This is a list that may be empty.",
                "",
                "OPTIONS",
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
