package net.jbock.examples;

import net.jbock.examples.fixture.ParserFixture;
import org.junit.Test;

public class TarArgumentsTest {

  private final ParserFixture<TarArguments> f =
      ParserFixture.create(TarArguments_Parser::parse);

  @Test
  public void testExtract() {
    f.assertThat("-x", "-f", "foo.tar").isParsedAs(
        "extract", true,
        "file", "foo.tar");
    f.assertThat("-v", "-x", "-f", "foo.tar").isParsedAs(
        "extract", true,
        "file", "foo.tar",
        "verbose", true);
  }

  @Test
  public void noGrouping() {
    f.assertThat("-v", "xf", "foo.tar").isInvalid("Invalid option: xf");
    f.assertThat("-v", "-xf", "foo.tar").isInvalid("Invalid option: -xf");
  }
}
