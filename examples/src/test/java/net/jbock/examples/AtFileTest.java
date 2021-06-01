package net.jbock.examples;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AtFileTest {

  @Test
  void testAtFileSyntax() {
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
    List<String> tokens = readAtFile(lines);
    assertEquals(4, tokens.size());
    assertEquals("", tokens.get(0));
    assertEquals("1", tokens.get(1));
    assertEquals("", tokens.get(2));
    assertEquals("2\" \\3  4 ", tokens.get(3));
  }

  private List<String> readAtFile(List<String> raw) {
    ArrayList<String> lines = new ArrayList<>(raw);
    Collections.reverse(lines);
    ArrayList<String> copy = new ArrayList<>();
    lines.stream().dropWhile(String::isEmpty).forEach(copy::add);
    Collections.reverse(copy);
    Iterator<String> it = copy.iterator();
    ArrayList<String> tokens = new ArrayList<>(copy.size());
    while (it.hasNext()) {
      tokens.add(readTokenFromAtFile(it));
    }
    return tokens;
  }

  private String readTokenFromAtFile(Iterator<String> it) {
    String result = it.next();
    StringBuilder sb = new StringBuilder();
    while (true) {
      boolean esc = false;
      for (int i = 0; i < result.length(); i++) {
        char c = result.charAt(i);
        if (c == '\\') {
          if (esc) {
            sb.append('\\');
            esc = false;
          } else esc = true;
        } else {
          if (esc) {
            if (c == 'n') sb.append('\n');
            else if (c == 'r') sb.append('\r');
            else if (c == 't') sb.append('\t');
            else sb.append(c);
            esc = false;
          } else sb.append(c);
        }
      }
      if (!esc || !it.hasNext()) break;
      result = it.next();
    }
    return sb.toString();
  }
}
