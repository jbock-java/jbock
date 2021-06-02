package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HelplessArgumentsTest {

  private final ParserTestFixture<HelplessArguments> f =
      ParserTestFixture.create(new HelplessArgumentsParser());

  @Test
  void success0() {
    HelplessArguments args = new HelplessArgumentsParser().parse(new String[]{"x"})
        .orElseThrow(notSuccess -> Assertions.<RuntimeException>fail("expecting success but was " + notSuccess));
    assertEquals("x", args.required());
    assertFalse(args.help());
  }

  @Test
  void success1() {
    HelplessArguments args = new HelplessArgumentsParser().parse(new String[]{"x", "--help"})
        .orElseThrow(notSuccess -> Assertions.<RuntimeException>fail("expecting success but was " + notSuccess));
    assertTrue(args.help());
    assertEquals("x", args.required());
  }

  @Test
  void success2() {
    HelplessArguments args = new HelplessArgumentsParser().parse(new String[]{"--help", "x"})
        .orElseThrow(notSuccess -> Assertions.<RuntimeException>fail("expecting success but was " + notSuccess));
    assertTrue(args.help());
    assertEquals("x", args.required());
  }

  @Test
  void errorNoArguments() {
    f.assertThat().failsWithMessage("Missing required parameter: REQUIRED");
  }

  @Test
  void errorInvalidOption() {
    f.assertThat("-p").failsWithMessage("Invalid option: -p");
  }
}
