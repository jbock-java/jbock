package net.jbock.examples;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AtFileTest {

  @Test
  void testAtFileSyntax() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    List<String> lines = new ArrayList<>();
    lines.add("");
    lines.add("1");
    lines.add("");
    lines.add("2\\\"\\ \\\\3\\");
    lines.add("  4 ");
    lines.add("");
    lines.add("");
    lines.add("");
    lines.add("");
    __AdditionArguments_Parser_AtFileReader reader = new __AdditionArguments_Parser_AtFileReader();
    Method readAtLines = reader.getClass().getDeclaredMethod("readAtLines", List.class);
    readAtLines.setAccessible(true);
    @SuppressWarnings("unchecked")
    List<String> tokens = (List<String>) readAtLines.invoke(reader, lines);
    assertEquals(4, tokens.size());
    assertEquals("", tokens.get(0));
    assertEquals("1", tokens.get(1));
    assertEquals("", tokens.get(2));
    assertEquals("2\" \\3  4 ", tokens.get(3));
  }
}
