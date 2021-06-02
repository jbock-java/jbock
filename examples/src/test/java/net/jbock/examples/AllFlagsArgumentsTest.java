package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

class AllFlagsArgumentsTest {

  private final ParserTestFixture<AllFlagsArguments> f =
      ParserTestFixture.create(new AllFlagsArgumentsParser());

  @Test
  void tests() {
    f.assertThat().succeeds("smallFlag", false);
    f.assertThat("--smallFlag").succeeds("smallFlag", true);
  }
}