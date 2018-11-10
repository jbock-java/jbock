package net.jbock.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import java.util.List;
import java.util.Optional;

import static com.squareup.javapoet.WildcardTypeName.subtypeOf;

public final class Util {

  static ParameterizedTypeName optionalOf(TypeName typeName) {
    return ParameterizedTypeName.get(ClassName.get(Optional.class), typeName);
  }

  static ParameterizedTypeName optionalOfSubtype(TypeName typeName) {
    return ParameterizedTypeName.get(ClassName.get(Optional.class), subtypeOf(typeName));
  }

  public static boolean hasDefaultConstructor(TypeElement classToCheck) {
    List<ExecutableElement> constructors = ElementFilter.constructorsIn(classToCheck.getEnclosedElements());
    if (constructors.isEmpty()) {
      return true;
    }
    for (ExecutableElement constructor : constructors) {
      if (!constructor.getParameters().isEmpty()) {
        continue;
      }
      if (constructor.getModifiers().contains(Modifier.PRIVATE)) {
        return false;
      }
      return constructor.getThrownTypes().isEmpty();
    }
    return false;
  }

  private enum CharType {
    LOWER, UPPER, DIGIT, OTHER

  }

  private static CharType charType(char c) {
    if (Character.isUpperCase(c)) {
      return CharType.UPPER;
    }
    if (Character.isLowerCase(c)) {
      return CharType.LOWER;
    }
    if (Character.isDigit(c)) {
      return CharType.DIGIT;
    }
    return CharType.OTHER;
  }

  static String snakeCase(String input) {
    int length = input.length();
    if (length < 2) {
      return input;
    }
    StringBuilder sb = new StringBuilder();
    CharType type0 = charType(input.charAt(0));
    CharType type1 = charType(input.charAt(1));
    sb.append(Character.toLowerCase(input.charAt(0)));
    if (type0 != type1) {
      sb.append('_');
    }
    sb.append(Character.toLowerCase(input.charAt(1)));
    for (int i = 2; i < length; i++) {
      char c = input.charAt(i);
      CharType type2 = charType(c);
      if (c != '_' &&
          type1 != type2 &&
          (type0 == type1 || type0 != type2)) {
        sb.append('_');
      }
      sb.append(Character.toLowerCase(c));
      type0 = type1;
      type1 = type2;
    }
    return sb.toString();
  }

  public static String snakeToCamel(String s) {
    StringBuilder sb = new StringBuilder();
    boolean upcase = false;
    boolean underscore = false;
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '_') {
        if (underscore) {
          sb.append('_');
        }
        underscore = true;
        upcase = true;
      } else {
        underscore = false;
        if (upcase) {
          sb.append(Character.toUpperCase(c));
          upcase = false;
        } else {
          sb.append(Character.toLowerCase(c));
        }
      }
    }
    return sb.toString();
  }
}
