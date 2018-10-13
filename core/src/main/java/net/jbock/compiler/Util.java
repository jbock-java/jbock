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

  public static ParameterizedTypeName optionalOf(TypeName typeName) {
    return ParameterizedTypeName.get(ClassName.get(Optional.class), typeName);
  }

  static ParameterizedTypeName optionalOfSubtype(TypeName typeName) {
    return ParameterizedTypeName.get(ClassName.get(Optional.class), subtypeOf(typeName));
  }

  public static boolean checkDefaultConstructorExists(TypeElement classToCheck) {
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


  static String snakeCase(String input) {
    StringBuilder sb = new StringBuilder();
    int prevLower = 0;
    int prevUpper = 0;
    for (int i = 0; i < input.length(); i++) {
      char c = input.charAt(i);
      if (Character.isUpperCase(c)) {
        if (prevLower >= 2) {
          sb.append('_');
        }
        prevLower = 0;
        prevUpper++;
      } else {
        if (prevUpper >= 2) {
          sb.append('_');
        }
        prevUpper = 0;
        prevLower++;
      }
      sb.append(Character.toLowerCase(c));
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
