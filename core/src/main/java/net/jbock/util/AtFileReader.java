package net.jbock.util;

import net.jbock.Command;
import net.jbock.either.Either;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static net.jbock.either.Either.left;
import static net.jbock.either.Either.right;

/**
 * <p>Read command line options from a configuration file.
 * The following escape sequences are recognized:</p>
 *
 * <br/>
 * <table>
 *   <caption>Escape sequences</caption>
 *   <thead><tr><td><b>Code</b></td><td><b>Meaning</b></td></tr></thead>
 *   <tr><td>{@code \\}</td><td>backslash</td></tr>
 *   <tr><td>{@code \n}</td><td>newline</td></tr>
 *   <tr><td>{@code \r}</td><td>carriage return</td></tr>
 *   <tr><td>{@code \t}</td><td>horizontal tab</td></tr>
 * </table>
 *
 * <p>An unpaired backslash at the end of a line prevents
 * the newline from being read.</p>
 * <p>Note: Even if set to {@code true},
 * and the user wants to pass exactly one positional parameter
 * that starts with an {@code @} character,
 * they can still prevent the {@code @file} expansion,
 * by passing {@code --} as the first token.</p>
 * <p>Note: additional arguments after the {@code @file}
 * are allowed and will be appended to the result.</p>
 */
public final class AtFileReader {

  /**
   * Read the contents of the {@code @file} into a list of Strings.
   * This method may be invoked from the generated code,
   * unless {@link Command#atFileExpansion()} is {@code false}.
   *
   * @param args command line input, must contain at least one token,
   *             and the first token must be two characters at least
   * @return the options in the file, or an error report
   */
  public Either<FileReadingError, List<String>> readAtFile(String[] args) {
    String fileName = args[0].substring(1);
    try {
      Path path = Paths.get(fileName);
      List<String> lines = Files.readAllLines(path);
      List<String> expanded = new ArrayList<>(readAtLines(lines));
      expanded.addAll(Arrays.asList(args).subList(1, args.length));
      return right(expanded);
    } catch (Exception e) {
      return left(new FileReadingError(e, fileName));
    }
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
