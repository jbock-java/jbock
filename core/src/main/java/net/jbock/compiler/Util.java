package net.jbock.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.util.Optional;
import java.util.StringJoiner;

import static com.squareup.javapoet.WildcardTypeName.subtypeOf;

public final class Util {

  public static ParameterizedTypeName optionalOf(TypeName typeName) {
    return ParameterizedTypeName.get(ClassName.get(Optional.class), typeName);
  }

  static ParameterizedTypeName optionalOfSubtype(TypeName typeName) {
    return ParameterizedTypeName.get(ClassName.get(Optional.class), subtypeOf(typeName));
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

  static String methodToString(ExecutableElement method) {
    StringJoiner joiner = new StringJoiner(", ", "(", ")");
    for (VariableElement variableElement : method.getParameters()) {
      joiner.add(variableElement.asType().toString());
    }
    return method.getSimpleName() + joiner.toString();
  }
}
