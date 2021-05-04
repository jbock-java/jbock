package net.jbock.examples;

import net.jbock.examples.CpArguments.Control;
import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CpArgumentsTest {

  private final ParserTestFixture<CpArguments> f =
      ParserTestFixture.create(new CpArguments_Parser());

  @Test
  void errorMissingSource() {
    f.assertThat("-r").failsWithMessage("Missing required: SOURCE");
  }

  @Test
  void enumValuesInMessage() {
    CpArguments_Parser.ParseResult result = new CpArguments_Parser().parse(new String[]{"a", "b", "--backup", "CLOUD"});
    Assertions.assertTrue(result instanceof CpArguments_Parser.ParsingFailed);
    CpArguments_Parser.ParsingFailed failure = (CpArguments_Parser.ParsingFailed) result;
    String message = failure.getError().getMessage();
    Assertions.assertEquals("No enum constant net.jbock.examples.CpArguments.Control.CLOUD [NONE, NUMBERED, EXISTING, SIMPLE]", message);
  }

  @Test
  void errorMissingDest() {
    f.assertThat("a").failsWithMessage("Missing required: DEST");
    f.assertThat("a", "-r").failsWithMessage("Missing required: DEST");
    f.assertThat("-r", "a").failsWithMessage("Missing required: DEST");
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
    f.assertThat("-a", "b").failsWithMessage("Invalid option: -a");
  }

  @Test
  void tooMany() {
    f.assertThat("a", "b", "c").failsWithMessage("Excess param: c");
  }

  @Test
  void tooManyAndFlag() {
    f.assertThat("-r", "a", "b", "c").failsWithMessage("Excess param: c");
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
    assertEquals(Optional.of(Control.NUMBERED), f.parse("a", "b", "--backup=NUMBERED").backup());
    f.assertThat("-r", "a", "b", "--backup", "SIMPLE")
        .succeeds(
            "source", "a",
            "dest", "b",
            "recursive", true,
            "backup", Optional.of(Control.SIMPLE),
            "suffix", Optional.empty());
  }

  @Test
  void testPrint() {
    f.assertPrintsHelp(
        "USAGE",
        "  cp-arguments [OPTION]... SOURCE DEST",
        "",
        "PARAMETERS",
        "  source              ",
        "  dest                ",
        "",
        "OPTIONS",
        "  -r, --r             ",
        "  --backup BACKUP     ",
        "  -s, --suffix SUFFIX  Override the usual backup suffix",
        "");
  }
}
