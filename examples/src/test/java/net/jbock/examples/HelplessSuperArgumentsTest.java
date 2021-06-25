package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import net.jbock.util.SuperResult;
import org.junit.jupiter.api.Test;

class HelplessSuperArgumentsTest {

  private final HelplessSuperArgumentsParser parser = new HelplessSuperArgumentsParser();

  private final ParserTestFixture<SuperResult<HelplessSuperArguments>> f =
      ParserTestFixture.create(parser::parse);

  @Test
  void testHelpDisabled() {
    f.assertThat("--help").fails("Invalid option: --help");
  }
}
