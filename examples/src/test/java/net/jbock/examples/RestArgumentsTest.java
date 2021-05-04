package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.jbock.examples.fixture.ParserTestFixture.assertArraysEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RestArgumentsTest {

  private final ParserTestFixture<RestArguments> f =
      ParserTestFixture.create(new RestArguments_Parser());

  private final Map<String, String> messages = new HashMap<>();

  private final String[] expected = {
      "USAGE",
      "  rest-arguments [OPTION]... [REST]...",
      "",
      "PARAMETERS",
      "  rest         Hello yes",
      "",
      "OPTIONS",
      "  --file FILE  This is dog",
      ""};

  @BeforeEach
  void setup() {
    messages.put("the.file", "This\nis\ndog\n");
    messages.put("the.rest", "Hello\n   yes\n");
  }

  @Test
  void testBundleKey() {
    String[] help = f.getHelp(messages);
    assertArraysEquals(expected, help);
  }

  @Test
  void testBundleKeyFromResourceBundle() {
    ResourceBundle bundle = mock(ResourceBundle.class);
    when(bundle.getKeys()).thenReturn(new Vector<>(messages.keySet()).elements());
    messages.forEach((k, v) -> when(bundle.getString(eq(k))).thenReturn(v));
    String[] help = f.getHelp(toMap(bundle));
    assertArraysEquals(expected, help);
  }

  private Map<String, String> toMap(ResourceBundle bundle) {
    return Collections.list(bundle.getKeys()).stream()
        .collect(Collectors.toMap(Function.identity(), bundle::getString));
  }
}
