package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Enumeration;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RestArgumentsTest {

  private ParserTestFixture<RestArguments> f =
      ParserTestFixture.create(RestArguments_Parser.create());

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
}
