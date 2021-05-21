package net.jbock.compiler;

import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Descriptions {

  private Descriptions() {
  }

  public static Optional<String> optionalString(String s) {
    if (s.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(s);
  }

  public static List<String> getDescription(
      Element element,
      Elements elements,
      String[] description) {
    if (description.length == 0) {
      return tokenizeJavadoc(elements.getDocComment(element));
    }
    return Arrays.asList(description);
  }

  private static List<String> tokenizeJavadoc(String docComment) {
    if (Objects.toString(docComment, "").trim().isEmpty()) {
      return List.of();
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
