package net.jbock.examples;

import net.jbock.examples.fixture.ParserFixture;
import org.junit.Test;

public class MvArgumentsTest {

  private final ParserFixture<MvArguments> f =
      ParserFixture.create(MvArguments_Parser::parse);

  @Test
  public void notEnoughArguments() {
    f.assertThat().isInvalid("Missing positional parameter: SOURCE");
    f.assertThat("a").isInvalid("Missing positional parameter: DEST");
  }

  @Test
  public void dashNotIgnored() {
    // see CommandLineArguments.ignoreDashes
    f.assertThat("-aa", "b").isInvalid("Invalid option: -aa");
  }

  @Test
  public void tooManyPositionalArguments() {
    f.assertThat("a", "b", "c").isInvalid("Excess option: c");
  }

  @Test
  public void validInvocation() {
    f.assertThat("a", "b").isParsedAs(
        "source", "a",
        "dest", "b");
  }
}
