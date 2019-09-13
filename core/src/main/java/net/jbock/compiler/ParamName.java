package net.jbock.compiler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParamName {

  private enum CharType {
    LOWER, UPPER, DIGIT, UNDERSCORE, OTHER
  }

  private final List<String> parts;

  private ParamName(List<String> parts) {
    this.parts = parts;
  }

  static ParamName create(String input) {
    int length = input.length();
    if (length < 2) {
      return new ParamName(Collections.singletonList(input));
    }
    List<String> result = new ArrayList<>();
    CharType type_ = charType(input.charAt(0));
    CharType type = charType(input.charAt(1));
    StringBuilder sb = new StringBuilder();
    if (type_ == CharType.LOWER &&
        (type == CharType.UPPER || type == CharType.DIGIT || type == CharType.UNDERSCORE)) {
      result.add(toLower(input.charAt(0)));
      sb.append(toLower(input.charAt(1)));
    } else {
      sb.append(toLower(input.charAt(0))).append(toLower(input.charAt(1)));
    }
    type_ = type;
    for (int i = 2; i < length; i++) {
      char c = input.charAt(i);
      type = charType(c);
      if (type_ == CharType.LOWER &&
          (type == CharType.UPPER || type == CharType.DIGIT || type == CharType.UNDERSCORE)) {
        result.add(sb.toString());
        sb.setLength(0);
      }
      sb.append(toLower(c));
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
