package net.jbock.compiler;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EnumName {

  private enum CharType {
    LOWER, UPPER, DIGIT, UNDERSCORE, OTHER, UNDEFINED
  }

  private final List<String> parts;

  private EnumName(List<String> parts) {
    this.parts = parts;
  }

  public static EnumName create(String input) {
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
    return new EnumName(result);
  }

  private static String toLower(char c) {
    if (c == '_') {
      return "";
    }
    return Character.toString(Character.toLowerCase(c));
  }

  EnumName append(String s) {
    List<String> newParts = new ArrayList<>(this.parts.size() + 1);
    newParts.addAll(this.parts);
    newParts.add(s);
    return new EnumName(newParts);
  }

  public String snake() {
    return snake('_');
  }

  public String snake(char delim) {
    return String.join(Character.toString(delim), parts);
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

  public String enumConstant() {
    return snake().toUpperCase(Locale.US);
  }
}
