package net.jbock.examples;

import net.jbock.examples.CpArguments.Control;
import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class CpArgumentsTest {

  private final CpArgumentsParser parser = new CpArgumentsParser();

  private final ParserTestFixture<CpArguments> f =
      ParserTestFixture.create(parser::parse);

  @Test
  void errorMissingSource() {
    f.assertThat("-r").fails("Missing required parameter SOURCE");
  }

  @Test
  void enumValuesInMessage() {
    f.assertThat("a", "b", "--backup", "CLOUD").fails(
        "while converting option BACKUP (--backup): No enum constant " +
            "net.jbock.examples.CpArguments.Control.CLOUD [NONE, NUMBERED, EXISTING, SIMPLE]");
  }

  @Test
  void errorMissingDest() {
    f.assertThat("a").fails("Missing required parameter DEST");
    f.assertThat("a", "-r").fails("Missing required parameter DEST");
    f.assertThat("-r", "a").fails("Missing required parameter DEST");
  }

  @Test
  void minimal() {
    f.assertThat("a", "b").succeeds(
        "source", "a",
        "dest", "b",
        "recursive", false,
        "backup", Optional.empty(),
        "suffix", Optional.empty());
    f.assertThat("b", "a").succeeds(
        "source", "b",
        "dest", "a",
        "recursive", false,
        "backup", Optional.empty(),
        "suffix", Optional.empty());
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
  void testNotClustering() {
    f.assertThat("-rs1", "a", "b").fails("Invalid token: -rs1");
  }

  @Test
  void flagInVariousPositions() {
    Object[] expected = new Object[]{
        "source", "a",
        "dest", "b",
        "recursive", true,
        "backup", Optional.empty(),
        "suffix", Optional.empty()};
    f.assertThat("-r", "a", "b")
        .succeeds(expected);
    f.assertThat("a", "-r", "b")
        .succeeds(expected);
    f.assertThat("a", "b", "-r")
        .succeeds(expected);
  }

  @Test
  void testEnum() {
    f.assertThat("a", "b", "--backup=NUMBERED").succeeds(
        "source", "a",
        "dest", "b",
        "recursive", false,
        "backup", Optional.of(Control.NUMBERED),
        "suffix", Optional.empty());
    f.assertThat("-r", "a", "b", "--backup", "SIMPLE").succeeds(
        "source", "a",
        "dest", "b",
        "recursive", true,
        "backup", Optional.of(Control.SIMPLE),
        "suffix", Optional.empty());
  }

  @Test
  void testPrint() {
    f.assertPrintsHelp(
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
