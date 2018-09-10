package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

class RestArgumentsTest {

  private ParserTestFixture<RestArguments> f =
      ParserTestFixture.create(RestArguments_Parser.create());

  @Test
  void testDashAllowed() {
    f.assertThat("-", "a").succeeds("file", emptyList(), "rest", asList("-", "a"));
  }

  @Test
  void testDoubleDashAllowed() {
    // -- has no special meaning
    f.assertThat("--", "a").succeeds("file", emptyList(), "rest", asList("--", "a"));
  }

  @Test
  void testMixed() {
    f.assertThat("--file=1", "--file", "2", "-", "-a", "--pq").succeeds(
        "file", asList("1", "2"),
        "rest", asList("-", "-a", "--pq"));
    f.assertThat("-", "--file=1", "-a", "--file", "2", "--pq").succeeds(
        "file", asList("1", "2"),
        "rest", asList("-", "-a", "--pq"));
  }

  @Test
  void testPrint() {
    f.assertPrints(
        "NAME",
        "  RestArguments",
        "",
        "SYNOPSIS",
        "  RestArguments [<options>] [<rest...>]",
        "",
        "DESCRIPTION",
        "",
        "REST",
        "",
        "OPTIONS",
        "  --file <file...>",
        "",
        "  --help",
        "    Print this help page.",
        "    The help flag may only be passed as the first argument.",
        "    Any further arguments will be ignored.",
        "",
        "");
  }
}
