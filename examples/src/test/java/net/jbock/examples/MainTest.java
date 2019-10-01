package net.jbock.examples;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainTest {

  @Test
  void testMain() {
    String[] argv = new String[]{"-v", "-f", "file.txt"};
    Main.Arguments args = Main_Arguments_Parser.create().parseOrExit(argv);
    assertEquals("file.txt", args.file());
    assertTrue(args.verbose());
  }
}
