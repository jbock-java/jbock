package net.jbock.examples;

import net.jbock.examples.fixture.TestOutputStream;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HelplessArgumentsTest {

  private String[] fullUsage = {
      "NAME",
      "       HelplessArguments",
      "",
      "SYNOPSIS",
      "       HelplessArguments [<options>] <REQUIRED>",
      "",
      "DESCRIPTION",
      "",
      "REQUIRED",
      "",
      "OPTIONS",
      "       --help",
      ""};

  @Test
  void success0() {
    HelplessArguments_Parser.ParseResult opt = HelplessArguments_Parser.create().parse(new String[]{"x"});
    assertTrue(opt.result().isPresent());
    HelplessArguments args = opt.result().get();
    assertEquals("x", args.required());
    assertFalse(args.help());
  }

  @Test
  void success1() {
    HelplessArguments_Parser.ParseResult opt = HelplessArguments_Parser.create().parse(new String[]{"x", "--help"});
    assertTrue(opt.result().isPresent());
    HelplessArguments args = opt.result().get();
    assertTrue(args.help());
    assertEquals("x", args.required());
  }

  @Test
  void success2() {
    HelplessArguments_Parser.ParseResult opt = HelplessArguments_Parser.create().parse(new String[]{"--help", "x"});
    assertTrue(opt.result().isPresent());
    HelplessArguments args = opt.result().get();
    assertTrue(args.help());
    assertEquals("x", args.required());
  }

  @Test
  void errorNoArguments() {
    TestOutputStream out = new TestOutputStream();
    HelplessArguments_Parser.create().withErrorStream(out.out).parse(new String[]{});
    String message = out.toString();
    assertTrue(message.startsWith(String.join("\n", fullUsage)));
    assertTrue(message.contains("Missing parameter: <REQUIRED>"));
  }

  @Test
  void errorInvalidOption() {
    TestOutputStream out = new TestOutputStream();
    HelplessArguments_Parser.create().withErrorStream(out.out).parse(new String[]{"-p"});
    String message = out.toString();
    assertTrue(message.startsWith(String.join("\n", fullUsage)));
    assertTrue(message.contains("Invalid option: -p"));
  }
}
