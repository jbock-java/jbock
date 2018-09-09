package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

class AllFlagsArgumentsTest {

  private ParserTestFixture<AllFlagsArguments> f =
      ParserTestFixture.create(AllFlagsArguments_Parser.create());

  @Test
  void tests() {
    f.assertThat().succeeds(
        "smallFlag", false,
        "bigFlag", false);
    f.assertThat("--smallFlag").succeeds(
        "smallFlag", true,
        "bigFlag", false);
    f.assertThat("--bigFlag").succeeds(
        "smallFlag", false,
        "bigFlag", true);
    f.assertThat("--smallFlag", "--bigFlag").succeeds(
        "smallFlag", true,
        "bigFlag", true);
  }
}