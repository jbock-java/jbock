package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

class PositionalArgumentsTest {

  private final PositionalArgumentsParser parser = new PositionalArgumentsParser();

  private final ParserTestFixture<PositionalArguments> f =
      ParserTestFixture.create(parser::parse);

  @Test
  void errorMissingParameters() {
    f.assertThat("a").fails("Missing required parameter DEST");
    f.assertThat("a", "b").fails("Missing required parameter ANOTHER_INT");
  }

  @Test
  void testRequiredOnly() {
    f.assertThat("a", "b", "1")
        .has(PositionalArguments::source, "a")
        .has(PositionalArguments::dest, "b")
        .has(PositionalArguments::anotherInt, 1)
        .has(PositionalArguments::optString, Optional.empty())
        .has(PositionalArguments::otherTokens, List.of());
  }

  @Test
  void testEmpty() {
    f.assertThat("", "", "0")
        .has(PositionalArguments::source, "")
        .has(PositionalArguments::dest, "")
        .has(PositionalArguments::anotherInt, 0)
        .has(PositionalArguments::optString, Optional.empty())
        .has(PositionalArguments::otherTokens, List.of());
  }

  @Test
  void testNoEscape() {
    f.assertThat("a", "b", "1", "c", "d")
        .has(PositionalArguments::source, "a")
        .has(PositionalArguments::dest, "b")
        .has(PositionalArguments::anotherInt, 1)
        .has(PositionalArguments::optString, Optional.of("c"))
        .has(PositionalArguments::otherTokens, List.of("d"));
  }

  @Test
  void testEscapeAllOPPositions() {
    f.assertThat("--", "-a", "-b", "-1", "-c", "-e")
        .has(PositionalArguments::source, "-a")
        .has(PositionalArguments::dest, "-b")
        .has(PositionalArguments::anotherInt, -1)
        .has(PositionalArguments::optString, Optional.of("-c"))
        .has(PositionalArguments::otherTokens, List.of("-e"));
    f.assertThat("a", "--", "-b", "-1", "-c", "-e")
        .has(PositionalArguments::source, "a")
        .has(PositionalArguments::dest, "-b")
        .has(PositionalArguments::anotherInt, -1)
        .has(PositionalArguments::optString, Optional.of("-c"))
        .has(PositionalArguments::otherTokens, List.of("-e"));
    f.assertThat("a", "b", "--", "-1", "-c", "-e")
        .has(PositionalArguments::source, "a")
        .has(PositionalArguments::dest, "b")
        .has(PositionalArguments::anotherInt, -1)
        .has(PositionalArguments::optString, Optional.of("-c"))
        .has(PositionalArguments::otherTokens, List.of("-e"));
    f.assertThat("a", "b", "1", "--", "-c", "-e")
        .has(PositionalArguments::source, "a")
        .has(PositionalArguments::dest, "b")
        .has(PositionalArguments::anotherInt, 1)
        .has(PositionalArguments::optString, Optional.of("-c"))
        .has(PositionalArguments::otherTokens, List.of("-e"));
    f.assertThat("a", "b", "1", "c", "--", "-e")
        .has(PositionalArguments::source, "a")
        .has(PositionalArguments::dest, "b")
        .has(PositionalArguments::anotherInt, 1)
        .has(PositionalArguments::optString, Optional.of("c"))
        .has(PositionalArguments::otherTokens, List.of("-e"));
    f.assertThat("a", "b", "1", "c", "e", "--")
        .has(PositionalArguments::source, "a")
        .has(PositionalArguments::dest, "b")
        .has(PositionalArguments::anotherInt, 1)
        .has(PositionalArguments::optString, Optional.of("c"))
        .has(PositionalArguments::otherTokens, List.of("e"));
  }

  @Test
  void testPrint() {
    f.assertPrintsHelp(
        "\u001B[1mUSAGE\u001B[m",
        "  positional-arguments SOURCE DEST ANOTHER_INT [OPT_STRING] OTHER_TOKENS...",
        "",
        "\u001B[1mPARAMETERS\u001B[m",
        "  SOURCE       ",
        "  DEST          Desc of dest.",
        "  ANOTHER_INT  ",
        "  OPT_STRING   ",
        "  OTHER_TOKENS ",
        "");
  }
}
