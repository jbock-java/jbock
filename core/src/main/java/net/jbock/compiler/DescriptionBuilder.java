package net.jbock.compiler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DescriptionBuilder {

  private DescriptionBuilder() {
  }

  public static Optional<String> optionalString(String s) {
    if (s.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(s);
  }

  public static String[] tokenizeJavadoc(String docComment) {
    if (docComment == null) {
      return new String[0];
    }
    String[] tokens = docComment.trim().split("\\R", -1);
    List<String> result = new ArrayList<>(tokens.length);
    for (String t : tokens) {
      String token = t.trim();
      if (token.startsWith("@")) {
        return result.toArray(new String[0]);
      }
      if (!token.isEmpty()) {
        result.add(token);
      }
    }
    return result.toArray(new String[0]);
  }
}
