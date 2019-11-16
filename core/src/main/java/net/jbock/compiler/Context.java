package net.jbock.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import net.jbock.CommandLineArguments;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Optional;

import static net.jbock.compiler.Constants.NONPRIVATE_ACCESS_MODIFIERS;

public final class Context {

  // the annotated class
  private final TypeElement sourceElement;

  // the class that will be generated
  private final ClassName generatedClass;

  // the abstract methods in the annotated class
  private final List<Param> parameters;

  // whether "--" is a special token
  private final boolean allowEscape;

  // whether "--help" is a special token
  private final boolean helpParameterEnabled;

  // program description from javadoc, can be overridden with bundle key jbock.description
  private final List<String> description;

  // program name from attribute
  private final String programName;

  // mission statement from attribute, can be overridden with bundle key jbock.mission
  private final String missionStatement;

  private Context(
      TypeElement sourceElement,
      ClassName generatedClass,
      List<Param> parameters,
      boolean allowEscape,
      boolean helpParameterEnabled,
      List<String> description,
      String programName,
      String missionStatement) {
    this.sourceElement = sourceElement;
    this.generatedClass = generatedClass;
    this.parameters = parameters;
    this.allowEscape = allowEscape;
    this.helpParameterEnabled = helpParameterEnabled;
    this.description = description;
    this.programName = programName;
    this.missionStatement = missionStatement;
  }

  static Context create(
      TypeElement sourceElement,
      ClassName generatedClass,
      List<Param> parameters,
      List<String> description,
      boolean allowEscape) {
    boolean addHelp = sourceElement.getAnnotation(CommandLineArguments.class).allowHelpOption();
    String missionStatement = sourceElement.getAnnotation(CommandLineArguments.class).missionStatement();

    return new Context(
        sourceElement,
        generatedClass,
        parameters,
        allowEscape,
        addHelp,
        description,
        programName(sourceElement),
        missionStatement);
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

  public boolean allowEscape() {
    return allowEscape;
  }

  public ClassName optionParserType() {
    return generatedClass.nestedClass("OptionParser");
  }

  public ClassName positionalOptionParserType() {
    return generatedClass.nestedClass("PositionalOptionParser");
  }

  public ClassName repeatableOptionParserType() {
    return generatedClass.nestedClass("RepeatableOptionParser");
  }

  public ClassName flagOptionParserType() {
    return generatedClass.nestedClass("FlagOptionParser");
  }

  public ClassName regularOptionParserType() {
    return generatedClass.nestedClass("RegularOptionParser");
  }

  public ClassName regularPositionalOptionParserType() {
    return generatedClass.nestedClass("RegularPositionalOptionParser");
  }

  public ClassName repeatablePositionalOptionParserType() {
    return generatedClass.nestedClass("RepeatablePositionalOptionParser");
  }

  public ClassName optionType() {
    return generatedClass.nestedClass("Option");
  }

  public ClassName parserStateType() {
    return generatedClass.nestedClass("ParserState");
  }

  public ClassName indentPrinterType() {
    return generatedClass.nestedClass("IndentPrinter");
  }

  public ClassName messagesType() {
    return generatedClass.nestedClass("Messages");
  }

  public ClassName implType() {
    return generatedClass.nestedClass(sourceElement.getSimpleName() + "Impl");
  }

  public ClassName tokenizerType() {
    return generatedClass.nestedClass("Tokenizer");
  }

  public ClassName parseResultType() {
    return generatedClass.nestedClass("ParseResult");
  }

  public ClassName parsingSuccessType() {
    return generatedClass.nestedClass("ParsingSuccess");
  }

  public ClassName parsingFailedType() {
    return generatedClass.nestedClass("ParsingFailed");
  }

  public Optional<ClassName> helpPrintedType() {
    return helpParameterEnabled ? Optional.of(generatedClass.nestedClass("HelpRequested")) : Optional.empty();
  }

  public TypeName sourceElement() {
    return TypeName.get(sourceElement.asType());
  }

  public Modifier[] getAccessModifiers() {
    return sourceElement.getModifiers().stream()
        .filter(NONPRIVATE_ACCESS_MODIFIERS::contains)
        .toArray(Modifier[]::new);
  }

  public ClassName generatedClass() {
    return generatedClass;
  }

  public List<Param> parameters() {
    return parameters;
  }

  public boolean isHelpParameterEnabled() {
    return helpParameterEnabled;
  }

  public List<String> description() {
    return description;
  }

  public String programName() {
    return programName;
  }

  public String missionStatement() {
    return missionStatement;
  }
}
