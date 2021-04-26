package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

class PsArgumentsTest {

  private final ParserTestFixture<PsArguments> f =
      ParserTestFixture.create(new PsArguments_Parser());

  @Test
  void testPrint() {
    f.assertPrintsHelp(
        "Usage: ps-arguments [options...]",
        "Options:",
        "  -a, --all         ",
        "  -w, --width WIDTH  This is the description.",
        "");
  }
}
