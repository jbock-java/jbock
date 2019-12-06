package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static net.jbock.examples.fixture.ParserTestFixture.assertArraysEquals;

class RestArgumentsTest {

  private ParserTestFixture<RestArguments> f =
      ParserTestFixture.create(RestArguments_Parser.create());

  @Test
  void testBundleKey() {
    Map<String, String> messages = new HashMap<>();
    messages.put("file", "Kawalski\nnext");
    messages.put("the.rest", "Hello\n   yes");
    String[] help = f.getHelp(messages);
    String[] expected = {
        "Usage: rest-arguments [options...] <rest>...",
        "rest             Hello yes",
        "    --file FILE  Kawalski next",
        ""
    };
    assertArraysEquals(expected, help);
  }
}
