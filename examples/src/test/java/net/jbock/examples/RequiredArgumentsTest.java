package net.jbock.examples;

import net.jbock.either.Either;
import net.jbock.examples.fixture.ParserTestFixture;
import net.jbock.util.HelpRequested;
import net.jbock.util.NotSuccess;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static java.util.Collections.emptyList;
import static net.jbock.examples.fixture.ParserTestFixture.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequiredArgumentsTest {

  private final RequiredArgumentsParser parser = new RequiredArgumentsParser();

  private final ParserTestFixture<RequiredArguments> f =
      ParserTestFixture.create(parser::parse);

  @Test
  void success() {
    f.assertThat("--dir", "A").succeeds("dir", "A", "otherTokens", emptyList());
  }

  @Test
  void errorDirMissing() {
    String[] emptyInput = new String[0];
    Either<NotSuccess, RequiredArguments> result = new RequiredArgumentsParser().parse(emptyInput);
    Assertions.assertTrue(result.getLeft().isPresent());
    Assertions.assertTrue(result.getLeft().get() instanceof HelpRequested);
  }

  @Test
  void errorRepeatedArgument() {
    assertTrue(parser.parse("--dir", "A", "--dir", "B").getLeft().map(f::castToError)
        .orElseThrow().message()
        .contains("Option '--dir' is a repetition"));
    assertTrue(parser.parse("--dir=A", "--dir", "B").getLeft().map(f::castToError)
        .orElseThrow().message()
        .contains("Option '--dir' is a repetition"));
    assertTrue(parser.parse("--dir=A", "--dir=B").getLeft().map(f::castToError)
        .orElseThrow().message()
        .contains("Option '--dir=B' is a repetition"));
    assertTrue(parser.parse("--dir", "A", "--dir=B").getLeft().map(f::castToError)
        .orElseThrow().message()
        .contains("Option '--dir=B' is a repetition"));
  }

  @Test
  void errorDetachedAttached() {
    assertTrue(parser.parse("--dir", "A", "--dir=B").getLeft().map(f::castToError)
        .orElseThrow().message()
        .contains("Option '--dir=B' is a repetition"));
  }

  @Test
  void testPrint() {
    String[] actual = parser.parse("--help")
        .getLeft().map(f::getUsageDocumentation).orElseThrow();
    assertEquals(actual,
        "\u001B[1mUSAGE\u001B[m",
        "  required-arguments --dir DIR OTHER_TOKENS...",
        "",
        "\u001B[1mPARAMETERS\u001B[m",
        "  OTHER_TOKENS ",
        "",
        "\u001B[1mOPTIONS\u001B[m",
        "  --dir DIR ",
        "");
  }
}
