package net.jbock.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AtFileReaderTest {

  private final AtFileReader reader = new AtFileReader();

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
    List<String> tokens = reader.readAtLines(lines);
    assertEquals(List.of(
        "1",
        "2\" \\3  4 "),
        tokens);
  }

  @Test
  void testNewline() {
    List<String> tokens = reader.readAtLines(List.of("\\n"));
    assertEquals(List.of("\n"), tokens);
  }

  @Test
  void testSingleQuotes() {
    List<String> tokens = reader.readAtLines(List.of("'\\n'"));
    assertEquals(List.of("\\n"), tokens);
  }
}