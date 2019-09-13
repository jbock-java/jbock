package net.jbock.compiler;

import java.util.ArrayList;
import java.util.List;

public class ParamName {

  private enum CharType {
    LOWER, UPPER, DIGIT, UNDERSCORE, OTHER, UNDEFINED
  }

  private final List<String> parts;

  private ParamName(List<String> parts) {
    this.parts = parts;
  }

  static ParamName create(String input) {
    List<String> result = new ArrayList<>();
    CharType type_ = CharType.UNDEFINED;
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < input.length(); i++) {
      CharType type = charType(input.charAt(i));
      if (type_ == CharType.LOWER &&
          (type == CharType.UPPER || type == CharType.DIGIT || type == CharType.UNDERSCORE)) {
        result.add(sb.toString());
        sb.setLength(0);
      }
      sb.append(toLower(input.charAt(i)));
      type_ = type;
    }
    if (sb.length() >= 1) {
      result.add(sb.toString());
    }
    return new ParamName(result);
  }

  private static String toLower(char c) {
    if (c == '_') {
      return "";
    }
    return Character.toString(Character.toLowerCase(c));
  }

  ParamName append(String s) {
    List<String> newParts = new ArrayList<>(this.parts.size() + 1);
    newParts.addAll(this.parts);
    newParts.add(s);
    return new ParamName(newParts);
  }

  public String snake() {
    return String.join("_", parts);
  }

  public String camel() {
    StringBuilder sb = new StringBuilder(parts.get(0));
    for (int i = 1; i < parts.size(); i++) {
      String part = parts.get(i);
      sb.append(Character.toUpperCase(part.charAt(0)));
      sb.append(part.substring(1));
    }
    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    return parts.equals(((ParamName) o).parts);
  }

  @Override
  public int hashCode() {
    return parts.hashCode();
  }

  private static CharType charType(char c) {
    if (c == '_') {
      return CharType.UNDERSCORE;
    }
    if (Character.isLowerCase(c)) {
      return CharType.LOWER;
    }
    if (Character.isUpperCase(c)) {
      return CharType.UPPER;
    }
    if (Character.isDigit(c)) {
      return CharType.DIGIT;
    }
    return CharType.OTHER;
  }
}
