package examples.dustin.commandline.jbock;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainTest {

  @Test
  void testMain() {
    String[] argv = new String[]{"-v", "-f", "file.txt"};
    Main.Arguments args = new Main_ArgumentsParser().parseOrExit(argv);
    assertEquals(Optional.of("file.txt"), args.file());
    assertTrue(args.verbose());
  }
}
