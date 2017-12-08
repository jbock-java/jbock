package net.jbock.compiler;

import static java.util.stream.Collectors.toList;
import static net.jbock.compiler.Util.asType;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.lang.model.element.TypeElement;
import net.jbock.com.squareup.javapoet.ClassName;

final class Context {

  // the annotated class
  final TypeElement sourceType;

  // the *_Parser class that will be generated
  final ClassName generatedClass;

  // corresponds to the abstract methods of the source type (in source order)
  final List<Param> parameters;

  // corresponds to the methods that have the Positional annotation (in source order)
  final List<Param> positionalParameters;

  // the stopword is either "--" or null
  final boolean stopword;

  // true if one method has the Positional annotation
  final boolean otherTokens;

  // true if upper-casing the method names would cause a naming conflict
  final boolean problematicOptionNames;

  // true if option grouping is allowed in the first argument
  final boolean grouping;

  // a set of all the param types in the sourceType
  final Set<Type> paramTypes;

  private Context(
      TypeElement sourceType,
      ClassName generatedClass,
      List<Param> parameters,
      List<Param> positionalParameters,
      boolean stopword,
      boolean otherTokens,
      boolean problematicOptionNames,
      boolean grouping,
      Set<Type> paramTypes) {
    this.sourceType = sourceType;
    this.generatedClass = generatedClass;
    this.parameters = parameters;
    this.positionalParameters = positionalParameters;
    this.stopword = stopword;
    this.otherTokens = otherTokens;
    this.problematicOptionNames = problematicOptionNames;
    this.grouping = grouping;
    this.paramTypes = paramTypes;
  }

  static Context create(
      TypeElement sourceType,
      List<Param> parameters,
      Set<Type> paramTypes,
      boolean grouping) {
    ClassName generatedClass = parserClass(ClassName.get(asType(sourceType)));
    boolean problematicOptionNames = problematicOptionNames(parameters);
    boolean otherTokens = paramTypes.contains(Type.POSITIONAL_LIST);
    boolean stopword = parameters.stream()
        .filter(param -> param.paramType == Type.POSITIONAL_LIST)
        .count() >= 2;
    List<Param> positionalParameters = parameters.stream().filter(p -> p.paramType.positional).collect(toList());
    return new Context(
        sourceType,
        generatedClass,
        parameters,
        positionalParameters,
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

  /**
   * @param j must be the Option index of a positional param
   * @return the positional index of the param that's specified by {@code j}.
   */
  int positionalIndex(int j) {
    Param param = parameters.get(j);
    for (int i = 0; i < positionalParameters.size(); i++) {
      Param p = positionalParameters.get(i);
      if (p.index == param.index) {
        return i;
      }
    }
    // can only happen if j is not the O-index of a positional param
    throw new AssertionError();
  }
}
