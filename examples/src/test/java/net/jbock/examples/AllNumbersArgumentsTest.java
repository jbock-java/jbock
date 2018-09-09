package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;

class AllNumbersArgumentsTest {

  private ParserTestFixture<AllNumbersArguments> f =
      ParserTestFixture.create(AllNumbersArguments_Parser.create());

  @Test
  void tests() {
    f.assertThat("-i1", "-i2", "-i2", "-i3").succeeds(
        "listOfIntegers", asList(1, 2, 2, 3));
  }
}