package net.jbock.compiler;

import static net.jbock.compiler.Util.asType;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.lang.model.element.TypeElement;
import net.jbock.CommandLineArguments;
import net.jbock.com.squareup.javapoet.ClassName;

final class Context {

  // the annotated class
  final TypeElement sourceType;

  // the *_Parser class that will be generated
  final ClassName generatedClass;

  // corresponds to the abstract methods of the source type
  final List<Param> parameters;

  // non-null if one method has the EverythingAfter annotation
  final String stopword;

  // true if one method has the OtherTokens annotation
  final boolean otherTokens;

  // true if upper-casing the method names may cause a naming conflict
  final boolean problematicOptionNames;

  // true if option grouping is allowed in the first argument
  final boolean grouping;

  private Context(
      TypeElement sourceType,
      ClassName generatedClass,
      List<Param> parameters,
      String stopword,
      boolean otherTokens,
      boolean problematicOptionNames,
      boolean grouping) {
    this.sourceType = sourceType;
    this.generatedClass = generatedClass;
    this.parameters = parameters;
    this.stopword = stopword;
    this.otherTokens = otherTokens;
    this.problematicOptionNames = problematicOptionNames;
    this.grouping = grouping;
  }

  static Context create(
      TypeElement sourceType,
      List<Param> parameters,
      String stopword) {
    ClassName generatedClass = parserClass(ClassName.get(asType(sourceType)));
    boolean otherTokens = parameters.stream()
        .anyMatch(p -> p.optionType == Type.OTHER_TOKENS);
    boolean problematicOptionNames = problematicOptionNames(parameters);
    boolean grouping = sourceType.getAnnotation(CommandLineArguments.class).grouping();
    return new Context(
        sourceType,
        generatedClass,
        parameters,
        stopword,
        otherTokens,
        problematicOptionNames,
        grouping);
  }

  private static boolean problematicOptionNames(List<Param> parameters) {
    Set<String> uppercaseArgumentNames = parameters.stream()
        .map(Param::parameterName)
        .map(Util::snakeCase)
        .collect(Collectors.toSet());
    return uppercaseArgumentNames.size() < parameters.size();
  }

  private static ClassName parserClass(ClassName type) {
    String name = String.join("_", type.simpleNames()) + "_Parser";
    return type.topLevelClassName().peerClass(name);
  }

  boolean everythingAfter() {
    return stopword != null;
  }
}
