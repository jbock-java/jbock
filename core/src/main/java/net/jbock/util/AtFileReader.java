package net.jbock.util;

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
    lines = new ArrayList<>(lines);
    Collections.reverse(lines);
    List<String> copy = new ArrayList<>(lines.size());
    lines.stream().dropWhile(String::isEmpty).forEach(copy::add);
    Collections.reverse(copy);
    Iterator<String> it = copy.iterator();
    List<String> tokens = new ArrayList<>(copy.size());
    while (it.hasNext()) {
      tokens.add(readTokenFromAtFile(it));
    }
    return tokens;
  }

  private String readTokenFromAtFile(Iterator<String> it) {
    String line = it.next();
    StringBuilder sb = new StringBuilder();
    while (true) {
      boolean esc = false;
      for (int i = 0; i < line.length(); i++) {
        char c = line.charAt(i);
        if (c == '\\') {
          if (esc) sb.append('\\');
          esc = !esc;
          continue;
        }
        if (esc) {
          if (c == 'n') sb.append('\n');
          else if (c == 'r') sb.append('\r');
          else if (c == 't') sb.append('\t');
          else sb.append(c);
          esc = false;
        } else sb.append(c);
      }
      if (!esc || !it.hasNext()) break;
      line = it.next();
    }
    return sb.toString();
  }
}
