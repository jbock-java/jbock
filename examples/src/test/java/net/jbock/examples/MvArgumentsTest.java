package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

class MvArgumentsTest {

  private ParserTestFixture<MvArguments> f =
      ParserTestFixture.create(MvArguments_Parser.create());

  @Test
  void notEnoughArguments() {
    f.assertThat().failsWithLine1("Missing parameter: <source>");
    f.assertThat("a").failsWithLine1("Missing parameter: <dest>");
  }

  @Test
  void dashNotIgnored() {
    // see CommandLineArguments.ignoreDashes
    f.assertThat("-aa", "b").failsWithLine1("Invalid option: -aa");
  }

  @Test
  void tooManyPositionalArguments() {
    f.assertThat("a", "b", "c").failsWithLine1("Invalid option: c");
  }

  @Test
  void validInvocation() {
    f.assertThat("a", "b").succeeds(
        "source", "a",
        "dest", "b");
  }

  @Test
  void testPrint() {
    f.assertPrints(
        "NAME",
        "  MvArguments",
        "",
        "SYNOPSIS",
        "  MvArguments <source> <dest>",
        "",
        "DESCRIPTION",
        "",
        "");
  }
}
