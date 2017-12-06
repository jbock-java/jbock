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

  // true if one method has the Positional annotation
  final boolean otherTokens;

  // true if upper-casing the method names may cause a naming conflict
  final boolean problematicOptionNames;

  // true if option grouping is allowed in the first argument
  final boolean grouping;

  final Set<Type> paramTypes;

  private Context(
      TypeElement sourceType,
      ClassName generatedClass,
      List<Param> parameters,
      String stopword,
      boolean otherTokens,
      boolean problematicOptionNames,
      boolean grouping,
      Set<Type> paramTypes) {
    this.sourceType = sourceType;
    this.generatedClass = generatedClass;
    this.parameters = parameters;
    this.stopword = stopword;
    this.otherTokens = otherTokens;
    this.problematicOptionNames = problematicOptionNames;
    this.grouping = grouping;
    this.paramTypes = paramTypes;
  }

  static Context create(
      TypeElement sourceType,
      List<Param> parameters,
      String stopword,
      Set<Type> paramTypes,
      boolean grouping) {
    ClassName generatedClass = parserClass(ClassName.get(asType(sourceType)));
    boolean problematicOptionNames = problematicOptionNames(parameters);
    boolean otherTokens = paramTypes.contains(Type.OTHER_TOKENS);
    return new Context(
        sourceType,
        generatedClass,
        parameters,
        stopword,
        otherTokens,
        problematicOptionNames,
        grouping,
        paramTypes);
  }

  private static boolean problematicOptionNames(List<Param> parameters) {
    Set<String> uppercaseArgumentNames = parameters.stream()
        .map(Param::methodName)
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
