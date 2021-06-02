package net.jbock.examples;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

public class AtFileTest {

  private final __AdditionArguments_Parser_AtFileReader reader =
      new __AdditionArguments_Parser_AtFileReader();

  @Test
  void testAtFileSyntax() {
    List<String> lines = List.of(
        "",
        "1",
        "",
        "2\\\"\\ \\\\3\\",
        "  4 ",
        "",
        "",
        "",
        "");
    List<String> tokens = invokeReadAtLines(lines);
    Assertions.assertEquals(List.of(
        "",
        "1",
        "",
        "2\" \\3  4 "),
        tokens);
  }

  private List<String> invokeReadAtLines(List<String> lines) {
    try {
      Method readAtLines = reader.getClass().getDeclaredMethod("readAtLines", List.class);
      readAtLines.setAccessible(true);
      @SuppressWarnings("unchecked")
      List<String> tokens = (List<String>) readAtLines.invoke(reader, lines);
      return tokens;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
