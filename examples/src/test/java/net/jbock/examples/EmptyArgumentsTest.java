package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class EmptyArgumentsTest {

  private ParserTestFixture<EmptyArguments> f =
      ParserTestFixture.create(EmptyArguments_Parser.create());

  @Test
  void name() {
    String[] argv = {};
    EmptyArguments args = f.parse(argv);
    assertNotNull(args);
  }

  @Test
  void testRejectAnything() {
    String[] argv = {"a"};
    f.assertThat(argv).failsWithLines(
        "Usage:",
        "  EmptyArguments",
        "",
        "Error:",
        "  Invalid option: a",
        "",
        "Try 'EmptyArguments --help' for more information.",
        "",
        "");
  }

  @Test
  void testPrintHelp() {
    f.assertPrintsHelp(
        "NAME",
        "  EmptyArguments",
        "",
        "SYNOPSIS",
        "  EmptyArguments",
        "",
        "DESCRIPTION",
        "",
        "OPTIONS",
        "  --help",
        "    Print this help page.",
        "    The help flag may only be passed as the first argument.",
        "    Any further arguments will be ignored.",
        "",
        "");
  }
}