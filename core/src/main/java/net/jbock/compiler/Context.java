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

  // corresponds to _all_ abstract methods of the source type (in source order, inheritance not considered)
  final List<Param> parameters;

  // only the methods that have the Positional annotation (in source order, inheritance not considered)
  final List<Param> positionalParameters;

  // the stopword is either "--" or null
  final boolean stopword;

  // true if upper-casing the method names would cause a naming conflict
  final boolean problematicOptionNames;

  // true if option grouping is allowed in the first argument
  final boolean grouping;

  // a set of only the non-positional param types in the sourceType
  final Set<Type> paramTypes;

  // a set of only the positional param types in the sourceType
  final Set<PositionalType> positionalParamTypes;

  private Context(
      TypeElement sourceType,
      ClassName generatedClass,
      List<Param> parameters,
      List<Param> positionalParameters,
      boolean stopword,
      boolean problematicOptionNames,
      boolean grouping,
      Set<Type> paramTypes,
      Set<PositionalType> positionalParamTypes) {
    this.sourceType = sourceType;
    this.generatedClass = generatedClass;
    this.parameters = parameters;
    this.positionalParameters = positionalParameters;
    this.stopword = stopword;
    this.problematicOptionNames = problematicOptionNames;
    this.grouping = grouping;
    this.paramTypes = paramTypes;
    this.positionalParamTypes = positionalParamTypes;
  }

  static Context create(
      TypeElement sourceType,
      List<Param> parameters,
      Set<Type> paramTypes,
      Set<PositionalType> positionalParamTypes,
      boolean grouping) {
    ClassName generatedClass = parserClass(ClassName.get(asType(sourceType)));
    boolean problematicOptionNames = problematicOptionNames(parameters);
    boolean stopword = positionalParamTypes.contains(PositionalType.POSITIONAL_LIST_2);
    List<Param> positionalParameters = parameters.stream().filter(p -> p.positionalType != null).collect(toList());
    return new Context(
        sourceType,
        generatedClass,
        parameters,
        positionalParameters,
        stopword,
        problematicOptionNames,
        grouping,
        paramTypes,
        positionalParamTypes);
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
   * @return the index in the list of all positional parameters, of the param that's specified by {@code j}.
   */
  int positionalIndex(int j) {
    Param param = parameters.get(j);
    for (int i = 0; i < positionalParameters.size(); i++) {
      Param p = positionalParameters.get(i);
      if (p.index == param.index) {
        return i;
      }
    }
    // j is not the Option index of a positional param.
    // Calling this method with such an argument is not allowed.
    throw new IllegalArgumentException(
        "Not a positional parameter: " + j);
  }

  /**
   * Determine how many positional arguments the user can specify at most,
   * before doubledash.
   *
   * @return the maximum number of positional arguments,
   *     or {@code -1} if there is no limit
   */
  int maxPositional() {
    if (positionalParamTypes.contains(PositionalType.POSITIONAL_LIST)) {
      return -1;
    }
    int count = 0;
    for (Param parameter : positionalParameters) {
      if (parameter.positionalType != PositionalType.POSITIONAL_LIST_2) {
        count++;
      }
    }
    return count;
  }
}
