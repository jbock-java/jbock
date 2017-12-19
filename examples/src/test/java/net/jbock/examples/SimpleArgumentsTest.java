package net.jbock.examples;

import net.jbock.examples.fixture.ParserFixture;
import org.junit.Test;

public class SimpleArgumentsTest {

  private final ParserFixture<SimpleArguments> f =
      ParserFixture.create(SimpleArguments_Parser::parse);

  @Test
  public void invalidOptions() {
    f.assertThat("xf", "1").isInvalid("Invalid option: xf");
    f.assertThat("-xf", "1").isInvalid("Invalid option: -xf");
  }
}
