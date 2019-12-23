package net.jbock.examples;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainTest {

  @Test
  void testMain() {
    String[] argv = new String[]{"-v", "-f", "file.txt"};
    Main.Arguments args = new Main_Arguments_Parser().parseOrExit(argv);
    assertEquals("file.txt", args.file());
    assertTrue(args.verbose());
  }
}
