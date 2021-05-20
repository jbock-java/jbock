package net.jbock.compiler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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

  public static List<String> tokenizeJavadoc(String docComment) {
    if (Objects.toString(docComment, "").trim().isEmpty()) {
      return Collections.emptyList();
    }
    String[] tokens = docComment.trim().split("\\R", -1);
    List<String> result = new ArrayList<>(tokens.length);
    for (String t : tokens) {
      String token = t.trim();
      if (token.startsWith("@")) {
        break;
      }
      if (!token.isEmpty()) {
        result.add(token);
      }
    }
    return result;
  }
}
