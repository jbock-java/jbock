package net.jbock.compiler;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class EnumName {

  private enum CharType {
    LOWER, UPPER, DIGIT, OTHER
  }

  private final List<String> parts; // lower case parts
  private final String enumConstant; // unique

  private EnumName(List<String> parts) {
    this(parts, makeCamel(parts));
  }

  private EnumName(List<String> parts, String enumConstant) {
    this.parts = parts;
    this.enumConstant = enumConstant;
  }

  private static final Set<CharType> BREAK_TYPES = EnumSet.of(
      CharType.UPPER, CharType.DIGIT);

  public static EnumName create(String input) {
    List<String> result = new ArrayList<>();
    CharType previousType = CharType.OTHER;
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < input.length(); i++) {
      CharType thisType = charType(input.charAt(i));
      if (sb.length() > 0 && (
          (previousType != CharType.UPPER && BREAK_TYPES.contains(thisType)))) {
        result.add(sb.toString().toLowerCase(Locale.US));
        sb.setLength(0);
      }
      sb.append(input.charAt(i));
      previousType = thisType;
    }
    if (sb.length() > 0) {
      result.add(sb.toString().toLowerCase(Locale.US));
    }
    return new EnumName(result);
  }

  public EnumName makeLonger() {
    String newConstant;
    if (charType(enumConstant.charAt(enumConstant.length() - 1)) == CharType.DIGIT) {
      newConstant = this.enumConstant + "1";
    } else {
      newConstant = this.enumConstant + "_1";
    }
    return new EnumName(parts, newConstant);
  }

  /**
   * Result may not be unique.
   * For example, {@code (a, b)} and {@code (a_b)}
   * produce the same snake-String when delim is {@code _}.
   */
  public String snake(char delim) {
    return String.join(Character.toString(delim), parts);
  }

  private static String makeCamel(List<String> myParts) {
    return String.join("_", myParts).toUpperCase(Locale.US);
  }

  private static CharType charType(char c) {
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
    return enumConstant;
  }
}
