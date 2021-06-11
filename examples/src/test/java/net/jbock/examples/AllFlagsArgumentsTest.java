package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

class AllFlagsArgumentsTest {

  private final AllFlagsArgumentsParser parser = new AllFlagsArgumentsParser();

  private final ParserTestFixture<AllFlagsArguments> f =
      ParserTestFixture.create(parser::parse);

  @Test
  void tests() {
    f.assertThat().succeeds("smallFlag", false);
    f.assertThat("--smallFlag").succeeds("smallFlag", true);
  }
}