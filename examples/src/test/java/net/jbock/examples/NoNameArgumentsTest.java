package net.jbock.examples;

import static java.util.Arrays.asList;

import net.jbock.CommandLineArguments;
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
        .succeeds(expected);
    f.assertThat("-n1", "--cmos", "--message=m", "--file=f", "--file=o", "--file=o")
        .succeeds(expected);
    f.assertThat("--file", "f", "--message=m", "--file", "o", "--cmos", "-n1", "--file", "o")
        .succeeds(expected);
  }

  @Test
  public void testFlag() {
    f.assertThat("--cmos", "-n1").succeeds(
        "cmos", true,
        "number", 1);
  }

  @Test
  public void testOptionalInt() {
    f.assertThat("-v", "1", "-n1").succeeds(
        "verbosity", 1,
        "number", 1);
    f.assertThat("-n1").succeeds(
        "number", 1);
  }

  @Test
  public void errorMissingInt() {
    f.assertThat("--cmos").failsWithLine1("Missing required option: NUMBER (-n, --number)");
  }

  @Test
  public void errorUnknownToken() {
    f.assertThat("blabla").failsWithLine1("Invalid option: blabla");
  }


  @Test
  public void testPrint() {
    f.assertPrints(
        "NAME",
        "  NoNameArguments",
        "",
        "SYNOPSIS",
        "  NoNameArguments [OPTION]... -n NUMBER",
        "",
        "DESCRIPTION",
        "",
        "  --message VALUE",
        "",
        "  --file VALUE...",
        "",
        "  -v, --verbosity NUMBER",
        "",
        "  -n, --number NUMBER",
        "",
        "  --cmos",
        "",
        "");
  }

}
