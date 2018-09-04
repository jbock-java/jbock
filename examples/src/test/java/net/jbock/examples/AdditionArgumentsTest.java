package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

class AdditionArgumentsTest {

  private ParserTestFixture<AdditionArguments> f =
      ParserTestFixture.create(AdditionArguments_Parser.create());

  @Test
  void optionalAbsent() {
    f.assertThat("1", "2").succeeds(
        "a", 1,
        "b", 2,
        "c", null);
  }

  @Test
  void optionalPresent() {
    f.assertThat("1", "2", "3").succeeds(
        "a", 1,
        "b", 2,
        "c", 3);
  }

  @Test
  void wrongNumber() {
    f.assertThat("-a", "2").failsWithLine1(
        "For input string: \"-a\"");
  }

  @Test
  void dashesIgnored() {
    f.assertThat("1", "-2", "3").satisfies(e -> e.sum() == 2);
    f.assertThat("-1", "-2", "-3").satisfies(e -> e.sum() == -6);
    f.assertThat("-1", "-2", "3").satisfies(e -> e.sum() == 0);
    f.assertThat("-1", "-2").satisfies(e -> e.sum() == -3);
  }


  @Test
  void testPrint() {
    f.assertPrints(
        "NAME",
        "  AdditionArguments",
        "",
        "SYNOPSIS",
        "  AdditionArguments <A> <B> [<c>]",
        "",
        "DESCRIPTION",
        "",
        "A",
        "  First argument",
        "",
        "B",
        "  Second argument",
        "",
        "C",
        "  Optional third argument",
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
