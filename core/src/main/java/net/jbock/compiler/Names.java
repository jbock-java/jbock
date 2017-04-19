package net.jbock.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import net.jbock.LongName;
import net.jbock.ShortName;

import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import java.util.List;
import java.util.regex.Pattern;

final class Names {

  final String shortName;
  final String longName;

  private static final Pattern WHITE_SPACE = Pattern.compile("^.*\\s+.*$");

  private Names(String shortName, String longName) {
    this.shortName = shortName;
    this.longName = longName;
  }

  static OptionType getOptionType(VariableElement variableElement) {
    TypeName type = TypeName.get(variableElement.asType());
    return getOptionType(type);
  }

  static OptionType getOptionType(TypeName type) {
    if (type.equals(TypeName.BOOLEAN)) {
      return OptionType.FLAG;
    }
    if (type.equals(Analyser.STRING)) {
      return OptionType.STRING;
    }
    if (isList(type)) {
      return OptionType.LIST;
    }
    return null;
  }

  static Names create(VariableElement variableElement) {
    LongName longName = variableElement.getAnnotation(LongName.class);
    ShortName shortName = variableElement.getAnnotation(ShortName.class);
    String ln = null, sn = null;
    TypeName type = TypeName.get(variableElement.asType());
    OptionType optionType = getOptionType(type);
    if (optionType == null) {
      throw new ValidationException(Diagnostic.Kind.ERROR,
          "Only String, boolean or List<String> allowed: " + variableElement.getSimpleName().toString(),
          variableElement);
    }
    if (optionType == OptionType.FLAG) {
      if (shortName != null) {
        sn = Character.toString(shortName.value());
      }
      if (longName != null) {
        ln = longName.value();
      }
      if (shortName == null && longName == null) {
        sn = variableElement.getSimpleName().toString();
      }
    } else {
      if (longName != null) {
        ln = longName.value();
      }
      if (shortName != null) {
        sn = Character.toString(shortName.value());
      }
      if (shortName == null && longName == null) {
        ln = variableElement.getSimpleName().toString();
      }
    }
    checkName(variableElement, sn);
    checkName(variableElement, ln);
    return new Names(sn, ln);
  }

  private static boolean isList(TypeName type) {
    if (!(type instanceof ParameterizedTypeName)) {
      return false;
    }
    ParameterizedTypeName t = (ParameterizedTypeName) type;
    if (t.rawType.equals(ClassName.get(List.class)) &&
        t.typeArguments.size() == 1 && t.typeArguments.get(0).equals(Analyser.STRING)) {
      return true;
    }
    return false;
  }

  private static void checkName(VariableElement parameter, String name) {
    if (name == null) {
      return;
    }
    if (name.isEmpty()) {
      throw new ValidationException(Diagnostic.Kind.ERROR, "The name may not be empty", parameter);
    }
    if (name.startsWith("-")) {
      throw new ValidationException(Diagnostic.Kind.ERROR, "The name may not start with '-'", parameter);
    }
    if (name.indexOf('=') >= 0) {
      throw new ValidationException(Diagnostic.Kind.ERROR, "The name may not contain '='", parameter);
    }
    if (WHITE_SPACE.matcher(name).matches()) {
      throw new ValidationException(Diagnostic.Kind.ERROR, "The name may not contain whitespace characters", parameter);
    }
  }
}
