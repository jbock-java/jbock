package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

class HelplessArgumentsTest {

  private final HelplessArgumentsParser parser = new HelplessArgumentsParser();

  private final ParserTestFixture<HelplessArguments> f =
      ParserTestFixture.create(parser::parse);

  @Test
  void success0() {
    f.assertThat("x")
        .has(HelplessArguments::required, "x")
        .has(HelplessArguments::help, false);
  }

  @Test
  void success1() {
    f.assertThat("x", "--help")
        .has(HelplessArguments::required, "x")
        .has(HelplessArguments::help, true);
  }

  @Test
  void success2() {
    f.assertThat("--help", "x")
        .has(HelplessArguments::required, "x")
        .has(HelplessArguments::help, true);
  }

  @Test
  void errorNoArguments() {
    f.assertThat(/* empty */)
        .fails("Missing required parameter REQUIRED");
  }

  @Test
  void errorInvalidOption() {
    f.assertThat("-p").fails("Invalid option: -p");
  }
}
