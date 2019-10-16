package net.jbock.compiler;

import com.squareup.javapoet.ClassName;
import net.jbock.CommandLineArguments;
import net.jbock.coerce.ParameterType;

import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;

public final class Context {

  // the annotated class
  private final TypeElement sourceElement;

  // the name of the class that will be generated
  private final ClassName generatedClass;

  // corresponds to all parameter methods
  private final List<Param> parameters;

  // number of methods that have the Positional annotation
  private final int numPositionalParameters;

  // whether "--" should end option parsing
  private final boolean allowEscape;

  // whether unknown tokens that start with dash should be accepted as positional parameters
  private final boolean strict;

  // true if --help is a special token
  private final boolean addHelp;

  // a set of only the non-positional param types in the sourceType
  private final Set<ParameterType> nonpositionalParamTypes;

  // a set of only the positional param types in the sourceType
  private final Set<ParameterType> positionalParamTypes;

  // program description from javadoc, can be overridden with bundle key jbock.description
  private final List<String> description;

  // program name from attribute
  private final String programName;

  // mission statement from attribute, can be overridden with bundle key jbock.mission
  private final String missionStatement;

  private final ClassName optionParserType;
  private final ClassName flagOptionParserType;
  private final ClassName repeatableOptionParserType;
  private final ClassName regularOptionParserType;
  private final ClassName positionalOptionParserType;
  private final ClassName regularPositionalOptionParserType;
  private final ClassName repeatablePositionalOptionParserType;
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
      ClassName positionalOptionParserType,
      ClassName flagOptionParserType,
      ClassName repeatableOptionParserType,
      ClassName regularOptionParserType,
      ClassName regularPositionalOptionParserType,
      ClassName repeatablePositionalOptionParserType,
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
    this.positionalOptionParserType = positionalOptionParserType;
    this.flagOptionParserType = flagOptionParserType;
    this.repeatableOptionParserType = repeatableOptionParserType;
    this.regularOptionParserType = regularOptionParserType;
    this.regularPositionalOptionParserType = regularPositionalOptionParserType;
    this.repeatablePositionalOptionParserType = repeatablePositionalOptionParserType;
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
    ClassName positionalOptionParserType = generatedClass.nestedClass("PositionalOptionParser");
    ClassName regularPositionalOptionParserType = generatedClass.nestedClass("RegularPositionalOptionParser");
    ClassName repeatablePositionalOptionParserType = generatedClass.nestedClass("RepeatablePositionalOptionParser");
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
        positionalOptionParserType,
        flagOptionParserType,
        repeatableOptionParserType,
        regularOptionParserType,
        regularPositionalOptionParserType,
        repeatablePositionalOptionParserType,
        optionType,
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
    if (!hasPositional()) {
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

  public ClassName optionParserType() {
    return optionParserType;
  }

  public ClassName positionalOptionParserType() {
    return positionalOptionParserType;
  }

  public ClassName repeatableOptionParserType() {
    return repeatableOptionParserType;
  }

  public ClassName flagOptionParserType() {
    return flagOptionParserType;
  }

  public ClassName regularOptionParserType() {
    return regularOptionParserType;
  }

  public ClassName regularPositionalOptionParserType() {
    return regularPositionalOptionParserType;
  }

  public ClassName repeatablePositionalOptionParserType() {
    return repeatablePositionalOptionParserType;
  }

  public ClassName optionType() {
    return optionType;
  }

  ClassName helperType() {
    return helperType;
  }

  public ClassName indentPrinterType() {
    return indentPrinterType;
  }

  public ClassName messagesType() {
    return messagesType;
  }

  ClassName implType() {
    return implType;
  }

  public ClassName tokenizerType() {
    return tokenizerType;
  }

  public ClassName parseResultType() {
    return parseResultType;
  }

  public ClassName successParseResultType() {
    return successParseResultType;
  }

  public ClassName errorParseResultType() {
    return errorParseResultType;
  }

  public ClassName helpPrintedParseResultType() {
    return helpPrintedParseResultType;
  }

  public TypeElement sourceElement() {
    return sourceElement;
  }

  public ClassName generatedClass() {
    return generatedClass;
  }

  List<Param> parameters() {
    return parameters;
  }

  boolean strict() {
    return strict;
  }

  public boolean addHelp() {
    return addHelp;
  }

  Set<ParameterType> nonpositionalParamTypes() {
    return nonpositionalParamTypes;
  }

  Set<ParameterType> positionalParamTypes() {
    return positionalParamTypes;
  }

  List<String> description() {
    return description;
  }

  String programName() {
    return programName;
  }

  String missionStatement() {
    return missionStatement;
  }
}
