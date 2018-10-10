package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Enumeration;
import java.util.ResourceBundle;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
  void testBundleJp() {
    ResourceBundle bundle = new ResourceBundle() {
      @Override
      protected Object handleGetObject(String key) {
        if ("param_file".equals(key)) {
          return "Sikorski";
        }
        return null;
      }

      @Override
      public Enumeration<String> getKeys() {
        return Collections.enumeration(Collections.singletonList("param_file"));
      }
    };
    String result = f.getHelp(bundle);
    assertTrue(result.contains("Sikorski"));

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
        "    This is the file.",
        "",
        "  --help",
        "    Print this help page.",
        "    The help flag may only be passed as the first argument.",
        "    Any further arguments will be ignored.",
        "",
        "");
  }
}
