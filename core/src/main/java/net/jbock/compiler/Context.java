package net.jbock.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import net.jbock.CommandLineArguments;
import net.jbock.coerce.ParameterType;

import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;

import static java.util.stream.Collectors.toList;

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

  // true if --help is a special token
  final boolean addHelp;

  // a set of only the non-positional param types in the sourceType
  final Set<ParameterType> nonpositionalParamTypes;

  // a set of only the positional param types in the sourceType
  final Set<ParameterType> positionalParamTypes;

  // program description from javadoc, can be overridden with bundle key jbock.description
  final List<String> description;

  // program name from attribute
  final String programName;

  // mission statement from attribute, can be overridden with bundle key jbock.mission
  final String missionStatement;

  private final ClassName optionParserType;
  private final ClassName flagOptionParserType;
  private final ClassName repeatableOptionParserType;
  private final ClassName regularOptionParserType;
  private final ClassName optionType;
  private final ClassName helperType;
  private final ClassName indentPrinterType;
  private final ClassName messagesType;
  private final ClassName implType;
  private final ClassName tokenizerType;
  private final ClassName parseResultType;
  private final ClassName successParseResultType;
  private final ClassName errorParseResultType;
  private final ClassName helpPrintedParseResultType;

  private Context(
      TypeElement sourceType,
      ClassName generatedClass,
      List<Param> parameters,
      List<Param> positionalParameters,
      boolean allowEscape,
      boolean strict,
      boolean addHelp,
      Set<ParameterType> nonpositionalParamTypes,
      Set<ParameterType> positionalParamTypes,
      List<String> description,
      String programName,
      String missionStatement,
      ClassName optionParserType,
      ClassName flagOptionParserType,
      ClassName repeatableOptionParserType,
      ClassName regularOptionParserType,
      ClassName optionType,
      ClassName helperType,
      ClassName indentPrinterType,
      ClassName messagesType,
      ClassName implType,
      ClassName tokenizerType,
      ClassName parseResultType,
      ClassName successParseResultType,
      ClassName errorParseResultType,
      ClassName helpPrintedParseResultType) {
    this.sourceType = sourceType;
    this.generatedClass = generatedClass;
    this.parameters = parameters;
    this.positionalParameters = positionalParameters;
    this.allowEscape = allowEscape;
    this.strict = strict;
    this.addHelp = addHelp;
    this.nonpositionalParamTypes = nonpositionalParamTypes;
    this.positionalParamTypes = positionalParamTypes;
    this.description = description;
    this.programName = programName;
    this.missionStatement = missionStatement;
    this.optionParserType = optionParserType;
    this.flagOptionParserType = flagOptionParserType;
    this.repeatableOptionParserType = repeatableOptionParserType;
    this.regularOptionParserType = regularOptionParserType;
    this.optionType = optionType;
    this.helperType = helperType;
    this.indentPrinterType = indentPrinterType;
    this.messagesType = messagesType;
    this.implType = implType;
    this.tokenizerType = tokenizerType;
    this.parseResultType = parseResultType;
    this.successParseResultType = successParseResultType;
    this.errorParseResultType = errorParseResultType;
    this.helpPrintedParseResultType = helpPrintedParseResultType;
  }

  static Context create(
      ClassName generatedClass,
      List<String> description,
      TypeElement sourceType,
      List<Param> parameters,
      Set<ParameterType> nonpositionalParamTypes,
      Set<ParameterType> positionalParamTypes) {
    boolean allowEscape = sourceType.getAnnotation(CommandLineArguments.class).allowEscapeSequence();
    List<Param> positionalParameters = parameters.stream().filter(Param::isPositional).collect(toList());
    boolean strict = !sourceType.getAnnotation(CommandLineArguments.class).allowPrefixedTokens();
    boolean addHelp = sourceType.getAnnotation(CommandLineArguments.class).allowHelpOption();
    String missionStatement = sourceType.getAnnotation(CommandLineArguments.class).missionStatement();
    ClassName optionType = generatedClass.nestedClass("Option");
    ClassName helperType = generatedClass.nestedClass("Helper");
    ClassName optionParserType = generatedClass.nestedClass("OptionParser");
    ClassName flagOptionParserType = generatedClass.nestedClass("FlagOptionParser");
    ClassName repeatableOptionParserType = generatedClass.nestedClass("RepeatableOptionParser");
    ClassName regularOptionParserType = generatedClass.nestedClass("RegularOptionParser");
    ClassName indentPrinterType = generatedClass.nestedClass("IndentPrinter");
    ClassName messagesType = generatedClass.nestedClass("Messages");
    ClassName implType = generatedClass.nestedClass(sourceType.getSimpleName() + "Impl");
    ClassName tokenizerType = generatedClass.nestedClass("Tokenizer");
    ClassName parseResultType = generatedClass.nestedClass("ParseResult");
    ClassName successParseResultType = generatedClass.nestedClass("ParsingSuccess");
    ClassName errorParseResultType = generatedClass.nestedClass("ParsingFailed");
    ClassName helpPrintedParseResultType = generatedClass.nestedClass("HelpPrinted");

    return new Context(
        sourceType,
        generatedClass,
        parameters,
        positionalParameters,
        allowEscape,
        strict,
        addHelp,
        nonpositionalParamTypes,
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
        messagesType,
        implType,
        tokenizerType,
        parseResultType,
        successParseResultType,
        errorParseResultType,
        helpPrintedParseResultType);
  }

  private static String programName(TypeElement sourceType) {
    CommandLineArguments annotation = sourceType.getAnnotation(CommandLineArguments.class);
    if (!annotation.programName().isEmpty()) {
      return annotation.programName();
    }
    switch (sourceType.getNestingKind()) {
      case MEMBER:
        return sourceType.getEnclosingElement().getSimpleName().toString();
      default:
        return sourceType.getSimpleName().toString();
    }
  }

  /**
   * @return the maximum number of positional arguments,
   * or {@code OptionalInt.empty()} if there is no limit
   */
  OptionalInt maxPositional() {
    if (positionalParamTypes.contains(ParameterType.REPEATABLE)) {
      return OptionalInt.empty();
    }
    return OptionalInt.of(positionalParameters.size());
  }

  boolean hasPositional() {
    return !positionalParameters.isEmpty();
  }

  boolean allowEscape() {
    return allowEscape && positionalParamTypes.contains(ParameterType.REPEATABLE);
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

  ClassName messagesType() {
    return messagesType;
  }

  ClassName implType() {
    return implType;
  }

  ClassName tokenizerType() {
    return tokenizerType;
  }

  ClassName parseResultType() {
    return parseResultType;
  }

  ClassName successParseResultType() {
    return successParseResultType;
  }

  ClassName errorParseResultType() {
    return errorParseResultType;
  }

  ClassName helpPrintedParseResultType() {
    return helpPrintedParseResultType;
  }

  boolean containsType(TypeName typeName) {
    for (Param parameter : parameters) {
      if (parameter.returnType().equals(typeName)) {
        return true;
      }
    }
    return false;
  }

  List<Param> positionalParameters() {
    return positionalParameters;
  }
}
