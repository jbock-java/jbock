package examples.dustin.commandline.jbock;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainTest {

  private final Main_ArgumentsParser parser = new Main_ArgumentsParser();

  @Test
  void testMain() {
    Main.Arguments args = parser.parse("-v", "-f", "file.txt")
        .orElseThrow(l -> Assertions.<RuntimeException>fail("expecting success but found: " + l));
    assertEquals(Optional.of("file.txt"), args.file());
    assertTrue(args.verbose());
  }
}
