package net.jbock.examples;

import net.jbock.examples.fixture.TestOutputStream;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HelplessArgumentsTest {

  @Test
  void success0() {
    HelplessArguments_Parser.ParseResult opt = HelplessArguments_Parser.create().parse(new String[]{"x"});
    assertTrue(opt instanceof HelplessArguments_Parser.ParsingSuccess);
    HelplessArguments args = ((HelplessArguments_Parser.ParsingSuccess) opt).result();
    assertEquals("x", args.required());
    assertFalse(args.help());
  }

  @Test
  void success1() {
    HelplessArguments_Parser.ParseResult opt = HelplessArguments_Parser.create().parse(new String[]{"x", "--help"});
    assertTrue(opt instanceof HelplessArguments_Parser.ParsingSuccess);
    HelplessArguments args = ((HelplessArguments_Parser.ParsingSuccess) opt).result();
    assertTrue(args.help());
    assertEquals("x", args.required());
  }

  @Test
  void success2() {
    HelplessArguments_Parser.ParseResult opt = HelplessArguments_Parser.create().parse(new String[]{"--help", "x"});
    assertTrue(opt instanceof HelplessArguments_Parser.ParsingSuccess);
    HelplessArguments args = ((HelplessArguments_Parser.ParsingSuccess) opt).result();
    assertTrue(args.help());
    assertEquals("x", args.required());
  }

  @Test
  void errorNoArguments() {
    TestOutputStream out = new TestOutputStream();
    HelplessArguments_Parser.create().withErrorStream(out.out).parse(new String[]{});
    String message = out.toString();
    assertTrue(message.contains("Missing parameter: <REQUIRED>"));
  }

  @Test
  void errorInvalidOption() {
    TestOutputStream out = new TestOutputStream();
    HelplessArguments_Parser.create().withErrorStream(out.out).parse(new String[]{"-p"});
    String message = out.toString();
    assertTrue(message.contains("Invalid option: -p"));
  }
}
