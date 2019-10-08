package net.jbock.compiler;

import com.squareup.javapoet.ClassName;
import net.jbock.CommandLineArguments;
import net.jbock.coerce.ParameterType;

import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;

final class Context {

  // the annotated class
  private final TypeElement sourceElement;

  // the name of the class that will be generated
  private final ClassName generatedClass;

  // corresponds to all parameter methods
  final List<Param> parameters;

  // number of methods that have the Positional annotation
  private final int numPositionalParameters;

  // whether "--" should end option parsing
  private final boolean allowEscape;

  // whether unknown tokens that start with dash should be accepted as positional parameters
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
      TypeElement sourceElement,
      ClassName generatedClass,
      List<Param> parameters,
      int numPositionalParameters,
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
    this.sourceElement = sourceElement;
    this.generatedClass = generatedClass;
    this.parameters = parameters;
    this.numPositionalParameters = numPositionalParameters;
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
      TypeElement sourceElement,
      ClassName generatedClass,
      List<Param> parameters,
      List<String> description,
      Set<ParameterType> nonpositionalParamTypes,
      Set<ParameterType> positionalParamTypes) {
    boolean allowEscape = sourceElement.getAnnotation(CommandLineArguments.class).allowEscapeSequence();
    long positionalParameters = parameters.stream().filter(Param::isPositional).count();
    boolean strict = positionalParameters > 0 && !sourceElement.getAnnotation(CommandLineArguments.class).allowPrefixedTokens();
    boolean addHelp = sourceElement.getAnnotation(CommandLineArguments.class).allowHelpOption();
    String missionStatement = sourceElement.getAnnotation(CommandLineArguments.class).missionStatement();
    ClassName optionType = generatedClass.nestedClass("Option");
    ClassName helperType = generatedClass.nestedClass("Helper");
    ClassName optionParserType = generatedClass.nestedClass("OptionParser");
    ClassName flagOptionParserType = generatedClass.nestedClass("FlagOptionParser");
    ClassName repeatableOptionParserType = generatedClass.nestedClass("RepeatableOptionParser");
    ClassName regularOptionParserType = generatedClass.nestedClass("RegularOptionParser");
    ClassName indentPrinterType = generatedClass.nestedClass("IndentPrinter");
    ClassName messagesType = generatedClass.nestedClass("Messages");
    ClassName implType = generatedClass.nestedClass(sourceElement.getSimpleName() + "Impl");
    ClassName tokenizerType = generatedClass.nestedClass("Tokenizer");
    ClassName parseResultType = generatedClass.nestedClass("ParseResult");
    ClassName successParseResultType = generatedClass.nestedClass("ParsingSuccess");
    ClassName errorParseResultType = generatedClass.nestedClass("ParsingFailed");
    ClassName helpPrintedParseResultType = generatedClass.nestedClass("HelpPrinted");

    return new Context(
        sourceElement,
        generatedClass,
        parameters,
        Long.valueOf(positionalParameters).intValue(),
        allowEscape,
        strict,
        addHelp,
        nonpositionalParamTypes,
        positionalParamTypes,
        description,
        programName(sourceElement),
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
    if (sourceType.getNestingKind() == NestingKind.MEMBER) {
      return sourceType.getEnclosingElement().getSimpleName().toString();
    }
    return sourceType.getSimpleName().toString();
  }

  /**
   * @return the maximum number of positional arguments,
   * or {@code OptionalInt.empty()} if there is no limit
   */
  OptionalInt maxPositional() {
    if (positionalParamTypes.contains(ParameterType.REPEATABLE)) {
      return OptionalInt.empty();
    }
    return OptionalInt.of(numPositionalParameters);
  }

  boolean hasPositional() {
    return numPositionalParameters > 0;
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

  TypeElement sourceElement() {
    return sourceElement;
  }

  ClassName generatedClass() {
    return generatedClass;
  }
}
