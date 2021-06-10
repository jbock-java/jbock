package net.jbock.examples;

import net.jbock.either.Either;
import net.jbock.examples.CpArguments.Control;
import net.jbock.examples.fixture.ParserTestFixture;
import net.jbock.util.ErrConvert;
import net.jbock.util.NotSuccess;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CpArgumentsTest {

  private final CpArgumentsParser parser = new CpArgumentsParser();

  private final ParserTestFixture<CpArguments> f =
      ParserTestFixture.create(parser);

  @Test
  void errorMissingSource() {
    Either<NotSuccess, CpArguments> result = parser.parse("-r");
    assertTrue(result.getLeft().map(f::castToError).orElseThrow().message()
        .contains("Missing required parameter SOURCE"));
  }

  @Test
  void enumValuesInMessage() {
    Either<NotSuccess, CpArguments> result = new CpArgumentsParser().parse("a", "b", "--backup", "CLOUD");
    assertTrue(result.getLeft().isPresent());
    assertTrue(result.getLeft().get() instanceof ErrConvert);
    ErrConvert error = (ErrConvert) result.getLeft().get();
    String message = error.message();
    assertEquals("while converting option BACKUP (--backup): No enum constant " +
        "net.jbock.examples.CpArguments.Control.CLOUD [NONE, NUMBERED, EXISTING, SIMPLE]", message);
  }

  @Test
  void errorMissingDest() {
    assertTrue(parser.parse("a").getLeft().map(f::castToError)
        .orElseThrow().message()
        .contains("Missing required parameter DEST"));
    assertTrue(parser.parse("a", "-r").getLeft().map(f::castToError)
        .orElseThrow().message()
        .contains("Missing required parameter DEST"));
    assertTrue(parser.parse("-r", "a").getLeft().map(f::castToError)
        .orElseThrow().message()
        .contains("Missing required parameter DEST"));
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
    assertTrue(parser.parse("-a", "b").getLeft().map(f::castToError)
        .orElseThrow().message()
        .contains("Invalid option: -a"));
  }

  @Test
  void tooMany() {
    assertTrue(parser.parse("a", "b", "c").getLeft().map(f::castToError)
        .orElseThrow().message()
        .contains("Excess param: c"));
  }

  @Test
  void tooManyAndFlag() {
    assertTrue(parser.parse("-r", "a", "b", "c").getLeft().map(f::castToError)
        .orElseThrow().message()
        .contains("Excess param: c"));
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
    String[] actual = parser.parse("--help")
        .getLeft().map(f::getUsageDocumentation).orElseThrow();
    ParserTestFixture.assertEquals(actual,
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
