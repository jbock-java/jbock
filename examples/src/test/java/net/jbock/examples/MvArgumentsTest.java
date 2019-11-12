package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

class MvArgumentsTest {

  private ParserTestFixture<MvArguments> f =
      ParserTestFixture.create(MvArguments_Parser.create());

  @Test
  void notEnoughArguments() {
    f.assertThat().failsWithUsageMessage("Missing parameter: <SOURCE>");
    f.assertThat("a").failsWithUsageMessage("Missing parameter: <DEST>");
  }

  @Test
  void invalidOption() {
    f.assertThat("-aa", "b").failsWithUsageMessage("Invalid option: -aa");
  }

  @Test
  void excessOption() {
    f.assertThat("a", "b", "c").failsWithUsageMessage("Invalid option: c");
  }

  @Test
  void invalidOptionEscapeSequenceThird() {
    f.assertThat("a", "b", "--", "c").failsWithUsageMessage("Invalid option: c");
  }

  @Test
  void validInvocation() {
    f.assertThat("a", "b").succeeds(
        "source", "a",
        "dest", "b");
  }

  @Test
  void valid() {
    f.assertThat("a", "b").succeeds(
        "source", "a",
        "dest", "b");
  }

  @Test
  void testPrint() {
    f.assertPrintsHelp(
        "NAME",
        "  MvArguments",
        "",
        "SYNOPSIS",
        "  MvArguments <SOURCE> <DEST>",
        "",
        "DESCRIPTION",
        "",
        "SOURCE",
        "",
        "DEST",
        "",
        "OPTIONS",
        "  --help",
        "    print online help",
        "",
        "");
  }
}
