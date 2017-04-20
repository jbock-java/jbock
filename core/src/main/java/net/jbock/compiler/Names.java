package net.jbock.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import net.jbock.OtherTokens;
import net.jbock.LongName;
import net.jbock.ShortName;

import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import java.util.List;
import java.util.regex.Pattern;

final class Names {

  final char shortName;
  final String longName;
  final OptionType optionType;

  private static final Pattern WHITE_SPACE = Pattern.compile("^.*\\s+.*$");

  private Names(Character shortName, String longName, OptionType optionType) {
    this.optionType = optionType;
    this.shortName = shortName == null ? ' ' : shortName;
    this.longName = longName;
  }

  private static OptionType getOptionType(TypeName type) {
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
    OtherTokens otherTokens = variableElement.getAnnotation(OtherTokens.class);
    TypeName type = TypeName.get(variableElement.asType());
    if (otherTokens != null) {
      if (shortName != null) {
        throw new ValidationException(Diagnostic.Kind.ERROR,
            "@OtherTokens may not be combined with @ShortName", variableElement);
      }
      if (longName != null) {
        throw new ValidationException(Diagnostic.Kind.ERROR,
            "@OtherTokens may not be combined with @LongName", variableElement);
      }
      if (!isList(type)) {
        throw new ValidationException(Diagnostic.Kind.ERROR,
            "@OtherTokens must be a java.util.List<String>", variableElement);
      }
      return new Names(null, variableElement.getSimpleName().toString(), OptionType.OTHER_TOKENS);
    }
    OptionType optionType = getOptionType(type);
    String ln = null;
    Character sn = null;
    if (optionType == null) {
      throw new ValidationException(Diagnostic.Kind.ERROR,
          String.format("Only String, boolean or java.util.List<String> allowed, but parameter %s has type %s",
              variableElement.getSimpleName(),
              TypeName.get(variableElement.asType())),
          variableElement);
    }
    if (longName != null) {
      ln = longName.value();
    }
    if (shortName != null) {
      sn = shortName.value();
    }
    if (shortName == null && longName == null) {
      ln = variableElement.getSimpleName().toString();
    }
    if (sn != null) {
      checkName(variableElement, Character.toString(sn));
    }
    checkName(variableElement, ln);
    return new Names(sn, ln, optionType);
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
