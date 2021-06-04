package net.jbock.util;

import net.jbock.Command;
import net.jbock.SuperCommand;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Read command line options from a configuration file.
 */
public final class AtFileReader {

  /**
   * Read file contents into a string array.
   * Public method that may be invoked from the generated code,
   * unless {@link Command#expandAtSign()} or
   * {@link SuperCommand#expandAtSign()} is {@code false}.
   *
   * @param fileName a file name
   * @return the options in the file
   * @throws IOException if an error occurs
   */
  public List<String> readAtFile(String fileName) throws IOException {
    Path path = Paths.get(fileName);
    List<String> lines = Files.readAllLines(path);
    return readAtLines(lines);
  }

  private List<String> readAtLines(List<String> lines) {
    Iterator<String> it = removeTrailingEmptyLines(lines);
    List<String> tokens = new ArrayList<>(lines.size());
    while (it.hasNext()) {
      tokens.add(readTokenFromAtFile(it));
    }
    return tokens;
  }

  private Iterator<String> removeTrailingEmptyLines(List<String> lines) {
    ArrayList<String> arrayLines = new ArrayList<>(lines);
    Collections.reverse(arrayLines);
    ArrayList<String> copy = new ArrayList<>(arrayLines.size());
    arrayLines.stream().dropWhile(String::isEmpty).forEach(copy::add);
    Collections.reverse(copy);
    return copy.iterator();
  }

  private String readTokenFromAtFile(Iterator<String> it) {
    StringBuilder sb = new StringBuilder();
    boolean esc;
    do {
      esc = readLine(it.next(), sb);
    } while (esc && it.hasNext());
    return sb.toString();
  }

  private boolean readLine(String line, StringBuilder sb) {
    boolean esc = false;
    int length = line.length();
    for (int i = 0; i < length; i++) {
      char c = line.charAt(i);
      if (c == '\\') {
        if (esc) {
          sb.append('\\');
          esc = false;
        } else {
          esc = true;
        }
      } else if (esc) {
        sb.append(escapeValue(c));
        esc = false;
      } else {
        sb.append(c);
      }
    }
    return esc;
  }

  private char escapeValue(char c) {
    switch (c) {
      case 'n':
        return '\n';
      case 'r':
        return '\r';
      case 't':
        return '\t';
      default:
        return c;
    }
  }
}
