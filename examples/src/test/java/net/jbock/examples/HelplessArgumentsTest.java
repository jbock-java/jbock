package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HelplessArgumentsTest {

  private final HelplessArgumentsParser parser = new HelplessArgumentsParser();

  private final ParserTestFixture<HelplessArguments> f =
      ParserTestFixture.create(parser);

  @Test
  void success0() {
    HelplessArguments args = parser.parse(new String[]{"x"})
        .orElseThrow(notSuccess -> Assertions.<RuntimeException>fail("expecting success but was " + notSuccess));
    assertEquals("x", args.required());
    assertFalse(args.help());
  }

  @Test
  void success1() {
    HelplessArguments args = parser.parse(new String[]{"x", "--help"})
        .orElseThrow(notSuccess -> Assertions.<RuntimeException>fail("expecting success but was " + notSuccess));
    assertTrue(args.help());
    assertEquals("x", args.required());
  }

  @Test
  void success2() {
    HelplessArguments args = parser.parse(new String[]{"--help", "x"})
        .orElseThrow(notSuccess -> Assertions.<RuntimeException>fail("expecting success but was " + notSuccess));
    assertTrue(args.help());
    assertEquals("x", args.required());
  }

  @Test
  void errorNoArguments() {
    String[] emptyInput = new String[0];
    assertTrue(parser.parse(emptyInput).getLeft().map(f::castToError)
        .orElseThrow().message()
        .contains("Missing required parameter: REQUIRED"));
  }

  @Test
  void errorInvalidOption() {
    assertTrue(parser.parse("-p").getLeft().map(f::castToError)
        .orElseThrow().message()
        .contains("Invalid option: -p"));
  }
}
