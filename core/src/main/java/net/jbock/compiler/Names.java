package net.jbock.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import net.jbock.ArgumentName;
import net.jbock.Description;
import net.jbock.EverythingAfter;
import net.jbock.LongName;
import net.jbock.OtherTokens;
import net.jbock.ShortName;

import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import java.util.List;
import java.util.regex.Pattern;

final class Names {

  final char shortName;
  final String longName;
  final OptionType optionType;
  final String parameterName;
  final String everythingAfter;

  final Description description;
  final ArgumentName argName;

  private static final Pattern WHITE_SPACE = Pattern.compile("^.*\\s+.*$");

  private Names(Character shortName,
                String longName,
                OptionType optionType,
                String parameterName,
                Description description,
                ArgumentName argName, String everythingAfter) {
    this.optionType = optionType;
    this.shortName = shortName == null ? ' ' : shortName;
    this.longName = longName;
    this.parameterName = parameterName;
    this.description = description;
    this.argName = argName;
    this.everythingAfter = everythingAfter;
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
    EverythingAfter everythingAfter = variableElement.getAnnotation(EverythingAfter.class);
    TypeName type = TypeName.get(variableElement.asType());
    if (otherTokens != null) {
      if (everythingAfter != null) {
        throw new ValidationException(Diagnostic.Kind.ERROR,
            "One argument may not have both @OtherTokens and @EverythingAfter", variableElement);
      }
      checkList(variableElement, type);
      return new Names(null, variableElement.getSimpleName().toString(), OptionType.OTHER_TOKENS,
          variableElement.getSimpleName().toString(),
          variableElement.getAnnotation(Description.class),
          variableElement.getAnnotation(ArgumentName.class),
          null);
    }
    if (everythingAfter != null) {
      checkList(variableElement, type);
      basicCheckName(variableElement, everythingAfter.value());
      return new Names(null, variableElement.getSimpleName().toString(), OptionType.EVERYTHING_AFTER,
          variableElement.getSimpleName().toString(),
          variableElement.getAnnotation(Description.class),
          variableElement.getAnnotation(ArgumentName.class),
          everythingAfter.value());
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
    return new Names(sn, ln, optionType, variableElement.getSimpleName().toString(),
        variableElement.getAnnotation(Description.class),
        variableElement.getAnnotation(ArgumentName.class), null);
  }

  private static void checkList(VariableElement variableElement, TypeName type) {
    if (!isList(type)) {
      throw new ValidationException(Diagnostic.Kind.ERROR,
          "Must be a java.util.List<String>", variableElement);
    }
  }

  private static boolean isList(TypeName type) {
    if (!(type instanceof ParameterizedTypeName)) {
      return false;
    }
    ParameterizedTypeName t = (ParameterizedTypeName) type;
    return t.rawType.equals(ClassName.get(List.class)) &&
        t.typeArguments.size() == 1 &&
        t.typeArguments.get(0).equals(Analyser.STRING);
  }

  private static void checkName(VariableElement parameter, String name) {
    if (name == null) {
      return;
    }
    basicCheckName(parameter, name);
    if (name.startsWith("-")) {
      throw new ValidationException(Diagnostic.Kind.ERROR, "The name may not start with '-'", parameter);
    }
    if (name.indexOf('=') >= 0) {
      throw new ValidationException(Diagnostic.Kind.ERROR, "The name may not contain '='", parameter);
    }
  }

  private static void basicCheckName(VariableElement parameter, String name) {
    if (name.isEmpty()) {
      throw new ValidationException(Diagnostic.Kind.ERROR, "The name may not be empty", parameter);
    }
    if (WHITE_SPACE.matcher(name).matches()) {
      throw new ValidationException(Diagnostic.Kind.ERROR, "The name may not contain whitespace characters", parameter);
    }
  }
}
