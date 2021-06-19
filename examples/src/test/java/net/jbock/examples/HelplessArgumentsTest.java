package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

class HelplessArgumentsTest {

  private final HelplessArgumentsParser parser = new HelplessArgumentsParser();

  private final ParserTestFixture<HelplessArguments> f =
      ParserTestFixture.create(parser::parse);

  @Test
  void success0() {
    f.assertThat("x").succeeds(
        "required", "x",
        "help", false);
  }

  @Test
  void success1() {
    f.assertThat("x", "--help").succeeds(
        "required", "x",
        "help", true);
  }

  @Test
  void success2() {
    f.assertThat("--help", "x").succeeds(
        "required", "x",
        "help", true);
  }

  @Test
  void errorNoArguments() {
    String[] emptyInput = new String[0];
    f.assertThat(emptyInput).fails("Missing required parameter REQUIRED");
  }

  @Test
  void errorInvalidOption() {
    f.assertThat("-p").fails("Invalid option: -p");
  }
}
