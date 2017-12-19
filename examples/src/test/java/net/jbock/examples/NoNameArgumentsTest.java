package net.jbock.examples;

import static java.util.Arrays.asList;

import net.jbock.examples.fixture.ParserFixture;
import org.junit.Test;

public class NoNameArgumentsTest {

  private final ParserFixture<NoNameArguments> f =
      ParserFixture.create(NoNameArguments_Parser::parse);

  @Test
  public void testDifferentOrder() {
    Object[] expected = {
        "message", "m",
        "cmos", true,
        "number", 1,
        "file", asList("f", "o", "o")};
    f.assertThat("--message=m", "--file=f", "--file=o", "--file=o", "--cmos", "-n1")
        .isParsedAs(expected);
    f.assertThat("-n1", "--cmos", "--message=m", "--file=f", "--file=o", "--file=o")
        .isParsedAs(expected);
    f.assertThat("--file", "f", "--message=m", "--file", "o", "--cmos", "-n1", "--file", "o")
        .isParsedAs(expected);
  }

  @Test
  public void testFlag() {
    f.assertThat("--cmos", "-n1").isParsedAs(
        "cmos", true,
        "number", 1);
  }

  @Test
  public void testOptionalInt() {
    f.assertThat("-v", "1", "-n1").isParsedAs(
        "verbosity", 1,
        "number", 1);
    f.assertThat("-n1").isParsedAs(
        "number", 1);
  }

  @Test
  public void errorMissingInt() {
    f.assertThat("--cmos").isInvalid("Missing required option: NUMBER (-n, --number)");
  }

  @Test
  public void errorUnknownToken() {
    f.assertThat("blabla").isInvalid("Invalid option: blabla");
  }
}
