package net.jbock.compiler;

import static net.jbock.compiler.Util.asType;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.lang.model.element.TypeElement;
import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.TypeName;

final class Context {

  final TypeElement sourceType;
  final ClassName generatedClass;
  final List<Param> parameters;
  final String stopword;
  final boolean otherTokens;
  final boolean rest;
  final boolean problematicOptionNames;

  private Context(
      TypeElement sourceType,
      ClassName generatedClass,
      List<Param> parameters,
      String stopword,
      boolean otherTokens,
      boolean rest,
      boolean problematicOptionNames) {
    this.sourceType = sourceType;
    this.generatedClass = generatedClass;
    this.parameters = parameters;
    this.stopword = stopword;
    this.otherTokens = otherTokens;
    this.rest = rest;
    this.problematicOptionNames = problematicOptionNames;
  }

  static Context create(
      TypeElement sourceType,
      List<Param> parameters,
      String stopword) {
    ClassName generatedClass = peer(ClassName.get(asType(sourceType)), Processor.SUFFIX);
    boolean otherTokens = parameters.stream()
        .anyMatch(p -> p.optionType == Type.OTHER_TOKENS);
    boolean rest = parameters.stream()
        .anyMatch(p -> p.optionType == Type.EVERYTHING_AFTER);
    boolean problematicOptionNames = problematicOptionNames(parameters);
    return new Context(
        sourceType,
        generatedClass,
        parameters,
        stopword,
        otherTokens,
        rest,
        problematicOptionNames);
  }

  private static boolean problematicOptionNames(List<Param> parameters) {
    Set<String> uppercaseArgumentNames = parameters.stream()
        .map(Param::parameterName)
        .map(Util::snakeCase)
        .collect(Collectors.toSet());
    return uppercaseArgumentNames.size() < parameters.size();
  }

  private static ClassName peer(ClassName type, String suffix) {
    String name = String.join("_", type.simpleNames()) + suffix;
    return type.topLevelClassName().peerClass(name);
  }

  public TypeName returnType() {
    return TypeName.get(sourceType.asType());
  }
}
