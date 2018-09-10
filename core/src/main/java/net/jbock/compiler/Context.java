package net.jbock.compiler;

import net.jbock.CommandLineArguments;
import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.ParameterizedTypeName;
import net.jbock.com.squareup.javapoet.TypeName;
import net.jbock.com.squareup.javapoet.WildcardTypeName;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;

import static java.util.stream.Collectors.toList;
import static javax.lang.model.util.ElementFilter.methodsIn;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Util.asType;

final class Context {

  // the annotated class
  final TypeElement sourceType;

  // the *_Parser class that will be generated
  final ClassName generatedClass;

  // corresponds to _all_ abstract methods of the source type (in source order, inheritance not considered)
  final List<Param> parameters;

  // only the methods that have the Positional annotation (in source order, inheritance not considered)
  private final List<Param> positionalParameters;

  // should "--" end option parsing
  private final boolean allowEscape;

  // should unknown parameters that start with dash be forbidden
  final boolean strict;

  // true if the source type does not already define toString
  final boolean generateToString;

  // true if --help is a special token
  final boolean addHelp;

  // a set of only the non-positional param types in the sourceType
  final Set<OptionType> nonpositionalParamTypes;

  // a set of only the positional param types in the sourceType
  final Set<OptionType> positionalParamTypes;

  // general usage information
  final List<String> overview;

  // general usage information
  final String programName;

  // general usage information
  final String missionStatement;

  private final ClassName optionParserType;
  private final ClassName flagOptionParserType;
  private final ClassName repeatableOptionParserType;
  private final ClassName regularOptionParserType;
  private final ClassName optionType;
  private final ClassName helperType;
  private final ClassName indentPrinterType;
  private final ClassName implType;
  private final ClassName tokenizerType;

  private final ParameterSpec quote = ParameterSpec.builder(
      ParameterizedTypeName.get(ClassName.get(Function.class), STRING, STRING), "quote").build();
  private final ParameterSpec toArray = ParameterSpec.builder(
      ParameterizedTypeName.get(ClassName.get(Collector.class),
          TypeName.get(CharSequence.class), WildcardTypeName.subtypeOf(Object.class), STRING), "toArray").build();

  private Context(
      TypeElement sourceType,
      ClassName generatedClass,
      List<Param> parameters,
      List<Param> positionalParameters,
      boolean allowEscape,
      boolean strict,
      boolean generateToString,
      boolean addHelp,
      Set<OptionType> nonpositionalParamTypes,
      Set<OptionType> positionalParamTypes,
      List<String> overview,
      String programName,
      String missionStatement,
      ClassName optionParserType,
      ClassName flagOptionParserType,
      ClassName repeatableOptionParserType,
      ClassName regularOptionParserType,
      ClassName optionType,
      ClassName helperType,
      ClassName indentPrinterType,
      ClassName implType,
      ClassName tokenizerType) {
    this.sourceType = sourceType;
    this.generatedClass = generatedClass;
    this.parameters = parameters;
    this.positionalParameters = positionalParameters;
    this.allowEscape = allowEscape;
    this.strict = strict;
    this.generateToString = generateToString;
    this.addHelp = addHelp;
    this.nonpositionalParamTypes = nonpositionalParamTypes;
    this.positionalParamTypes = positionalParamTypes;
    this.overview = overview;
    this.programName = programName;
    this.missionStatement = missionStatement;
    this.optionParserType = optionParserType;
    this.flagOptionParserType = flagOptionParserType;
    this.repeatableOptionParserType = repeatableOptionParserType;
    this.regularOptionParserType = regularOptionParserType;
    this.optionType = optionType;
    this.helperType = helperType;
    this.indentPrinterType = indentPrinterType;
    this.implType = implType;
    this.tokenizerType = tokenizerType;
  }

  static Context create(
      TypeElement sourceType,
      List<Param> parameters,
      Set<OptionType> paramTypes,
      Set<OptionType> positionalParamTypes) {
    ClassName generatedClass = parserClass(ClassName.get(asType(sourceType)));
    boolean allowEscape = sourceType.getAnnotation(CommandLineArguments.class).allowEscape();
    List<Param> positionalParameters = parameters.stream().filter(Param::isPositional).collect(toList());
    boolean strict = sourceType.getAnnotation(CommandLineArguments.class).strict();
    boolean addHelp = sourceType.getAnnotation(CommandLineArguments.class).addHelp();
    boolean generateToString = methodsIn(sourceType.getEnclosedElements()).stream()
        .filter(method -> method.getParameters().isEmpty())
        .map(ExecutableElement::getSimpleName)
        .map(Name::toString)
        .noneMatch(s -> s.equals("toString"));
    List<String> description = Arrays.asList(sourceType.getAnnotation(CommandLineArguments.class).overview());
    String missionStatement = sourceType.getAnnotation(CommandLineArguments.class).missionStatement();
    ClassName optionType = generatedClass.nestedClass("Option");
    ClassName helperType = generatedClass.nestedClass("Helper");
    ClassName optionParserType = generatedClass.nestedClass("OptionParser");
    ClassName flagOptionParserType = generatedClass.nestedClass("FlagOptionParser");
    ClassName repeatableOptionParserType = generatedClass.nestedClass("RepeatableOptionParser");
    ClassName regularOptionParserType = generatedClass.nestedClass("RegularOptionParser");
    ClassName indentPrinterType = generatedClass.nestedClass("IndentPrinter");
    ClassName implType = generatedClass.nestedClass(sourceType.getSimpleName() + "Impl");
    ClassName tokenizerType = generatedClass.nestedClass("Tokenizer");

    return new Context(
        sourceType,
        generatedClass,
        parameters,
        positionalParameters,
        allowEscape,
        strict,
        generateToString,
        addHelp,
        paramTypes,
        positionalParamTypes,
        description,
        programName(sourceType),
        missionStatement,
        optionParserType,
        flagOptionParserType,
        repeatableOptionParserType,
        regularOptionParserType, optionType,
        helperType,
        indentPrinterType,
        implType,
        tokenizerType);
  }

  private static ClassName parserClass(ClassName type) {
    String name = String.join("_", type.simpleNames()) + "_Parser";
    return type.topLevelClassName().peerClass(name);
  }

  private static String programName(TypeElement sourceType) {
    CommandLineArguments annotation = sourceType.getAnnotation(CommandLineArguments.class);
    if (!annotation.programName().isEmpty()) {
      return annotation.programName();
    }
    switch (sourceType.getNestingKind()) {
      case MEMBER:
        return Util.asType(sourceType.getEnclosingElement()).getSimpleName().toString();
      default:
        return sourceType.getSimpleName().toString();
    }
  }

  /**
   * @param j must be the Option index of a positional param
   * @return the index in the list of all positional parameters, of the param that's specified by {@code j}.
   */
  int positionalIndex(int j) {
    Param param = parameters.get(j);
    if (!param.isPositional()) {
      return -1;
    }
    for (int i = 0; i < positionalParameters.size(); i++) {
      Param p = positionalParameters.get(i);
      if (p.index == param.index) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Determine how many positional arguments the user can specify at most,
   * before doubledash.
   *
   * @return the maximum number of positional arguments,
   * or {@code OptionalInt.empty()} if there is no limit
   */
  OptionalInt maxPositional() {
    if (positionalParameters.isEmpty()) {
      return OptionalInt.empty();
    }
    if (positionalParamTypes.contains(OptionType.REPEATABLE)) {
      return OptionalInt.empty();
    }
    return OptionalInt.of(positionalParameters.size());
  }

  boolean hasPositional() {
    return !positionalParameters.isEmpty();
  }

  boolean allowEscape() {
    return allowEscape && positionalParamTypes.contains(OptionType.REPEATABLE);
  }

  ClassName optionParserType() {
    return optionParserType;
  }

  ClassName repeatableOptionParserType() {
    return repeatableOptionParserType;
  }

  ClassName flagOptionParserType() {
    return flagOptionParserType;
  }

  ClassName regularOptionParserType() {
    return regularOptionParserType;
  }

  ClassName optionType() {
    return optionType;
  }

  ClassName helperType() {
    return helperType;
  }

  ClassName indentPrinterType() {
    return indentPrinterType;
  }

  ClassName implType() {
    return implType;
  }

  ClassName tokenizerType() {
    return tokenizerType;
  }

  ParameterSpec quoteParam() {
    return quote;
  }

  ParameterSpec toArrayParam() {
    return toArray;
  }

  boolean containsType(TypeName typeName) {
    for (Param parameter : parameters) {
      if (parameter.returnType().equals(typeName)) {
        return true;
      }
    }
    return false;
  }
}
