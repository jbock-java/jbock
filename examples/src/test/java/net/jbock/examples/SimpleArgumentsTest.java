package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static net.jbock.examples.fixture.ParserTestFixture.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimpleArgumentsTest {

  private final SimpleArgumentsParser parser = new SimpleArgumentsParser();

  private final ParserTestFixture<SimpleArguments> f =
      ParserTestFixture.create(parser);

  @Test
  void invalidOptions() {
    assertTrue(parser.parse("xf", "1").getLeft().map(f::castToError)
        .orElseThrow().message()
        .contains("Excess param: xf"));
    assertTrue(parser.parse("-xf", "1").getLeft().map(f::castToError)
        .orElseThrow().message()
        .contains("Invalid token: -xf"));
  }

  @Test
  void success() {
    f.assertThat("--file", "1").succeeds("extract", false, "file", Optional.of("1"));
  }

  @Test
  void errorHelpNotFirstArgument() {
    assertTrue(parser.parse("--file", "1", "--help").getLeft().map(f::castToError)
        .orElseThrow().message()
        .contains("Invalid option: --help"));
  }

  @Test
  void testPrint() {
    String[] actual = parser.parse("--help")
        .getLeft().map(f::getUsageDocumentation).orElseThrow();
    assertEquals(actual,
        "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
        "",
        "\u001B[1mUSAGE\u001B[m",
        "  simple-arguments [OPTIONS]",
        "",
        "\u001B[1mOPTIONS\u001B[m",
        "  -x, --x      aa",
        "               AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
        "               AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
        "               aa aa",
        "  --file FILE ",
        "");
  }
}
