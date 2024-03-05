package net.jbock.examples;

import net.jbock.examples.CpArguments.Control;
import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class CpArgumentsTest {

    private final ParserTestFixture<CpArguments> f =
            ParserTestFixture.create(CpArgumentsParser::parse);

    @Test
    void errorMissingSource() {
        f.assertThat("-r").fails("Missing required parameter SOURCE");
    }

    @Test
    void enumValuesInMessage() {
        f.assertThat("a", "b", "--backup", "CLOUD").fails(
                "while converting option BACKUP (--backup): No such constant: CLOUD\n" +
                        "Possible values (ignoring case):\n" +
                        "  NONE\n" +
                        "  NUMBERED\n" +
                        "  EXISTING\n" +
                        "  SIMPLE\n");
    }

    @Test
    void errorMissingDest() {
        f.assertThat("a").fails("Missing required parameter DEST");
        f.assertThat("a", "-r").fails("Missing required parameter DEST");
        f.assertThat("-r", "a").fails("Missing required parameter DEST");
    }

    @Test
    void singleDashParameter() {
        f.assertThat("a", "-")
                .has(CpArguments::source, "a")
                .has(CpArguments::dest, "-")
                .has(CpArguments::recursive, false)
                .has(CpArguments::backup, Optional.empty())
                .has(CpArguments::suffix, Optional.empty());
    }

    @Test
    void dashNotIgnored() {
        f.assertThat("-a", "b").fails("Invalid option: -a");
    }

    @Test
    void tooMany() {
        f.assertThat("a", "b", "c").fails("Excess param: c");
    }

    @Test
    void tooManyAndFlag() {
        f.assertThat("-r", "a", "b", "c").fails("Excess param: c");
    }

    @Test
    void flagInVariousPositions() {
        f.assertThat("-r", "a", "b")
                .has(CpArguments::source, "a")
                .has(CpArguments::dest, "b")
                .has(CpArguments::recursive, true)
                .has(CpArguments::backup, Optional.empty())
                .has(CpArguments::suffix, Optional.empty());
        f.assertThat("a", "-r", "b")
                .has(CpArguments::source, "a")
                .has(CpArguments::dest, "b")
                .has(CpArguments::recursive, true)
                .has(CpArguments::backup, Optional.empty())
                .has(CpArguments::suffix, Optional.empty());
        f.assertThat("a", "b", "-r")
                .has(CpArguments::source, "a")
                .has(CpArguments::dest, "b")
                .has(CpArguments::recursive, true)
                .has(CpArguments::backup, Optional.empty())
                .has(CpArguments::suffix, Optional.empty());
    }

    @Test
    void testEnum() {
        f.assertThat("a", "b", "--backup=NUMBERED")
                .has(CpArguments::source, "a")
                .has(CpArguments::dest, "b")
                .has(CpArguments::recursive, false)
                .has(CpArguments::backup, Optional.of(Control.NUMBERED))
                .has(CpArguments::suffix, Optional.empty());
        f.assertThat("-r", "a", "b", "--backup", "SIMPLE")
                .has(CpArguments::source, "a")
                .has(CpArguments::dest, "b")
                .has(CpArguments::recursive, true)
                .has(CpArguments::backup, Optional.of(Control.SIMPLE))
                .has(CpArguments::suffix, Optional.empty());
    }

    @Test
    void testPrint() {
        f.assertPrintsHelp(
                CpArgumentsParser.createModel(),
                "\u001B[1mUSAGE\u001B[m",
                "  cp-arguments [OPTIONS] SOURCE DEST",
                "",
                "\u001B[1mPARAMETERS\u001B[m",
                "  SOURCE ",
                "  DEST   ",
                "",
                "\u001B[1mOPTIONS\u001B[m",
                "  -r, --r             ",
                "  --backup BACKUP     ",
                "  -s, --suffix SUFFIX  Override the usual backup suffix",
                "");
    }
}
