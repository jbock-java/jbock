package net.jbock.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import net.jbock.Command;

import javax.lang.model.element.Modifier;
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
  private final List<Parameter> parameters;

  // whether "--" is a special token
  private final boolean allowEscape;

  // whether "--help" is a special token
  private final boolean helpParameterEnabled;

  // program name from attribute
  private final String programName;

  private Context(
      TypeElement sourceElement,
      ClassName generatedClass,
      List<Parameter> parameters,
      boolean allowEscape,
      boolean helpParameterEnabled,
      String programName) {
    this.sourceElement = sourceElement;
    this.generatedClass = generatedClass;
    this.parameters = parameters;
    this.allowEscape = allowEscape;
    this.helpParameterEnabled = helpParameterEnabled;
    this.programName = programName;
  }

  static Context create(
      TypeElement sourceElement,
      ClassName generatedClass,
      List<Parameter> parameters,
      boolean allowEscape) {
    Command annotation = sourceElement.getAnnotation(Command.class);
    boolean helpParameterEnabled = !annotation.helpDisabled();

    return new Context(
        sourceElement,
        generatedClass,
        parameters,
        allowEscape,
        helpParameterEnabled,
        programName(sourceElement));
  }

  private static String programName(TypeElement sourceType) {
    Command annotation = sourceType.getAnnotation(Command.class);
    if (!annotation.value().isEmpty()) {
      return annotation.value();
    }
    String simpleName = sourceType.getSimpleName().toString();
    return ParamName.create(simpleName).snake('-');
  }

  public boolean allowEscape() {
    return allowEscape;
  }

  public ClassName optionParserType() {
    return generatedClass.nestedClass("OptionParser");
  }

  public ClassName paramParserType() {
    return generatedClass.nestedClass("ParamParser");
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

  public ClassName regularParamParserType() {
    return generatedClass.nestedClass("RegularParamParser");
  }

  public ClassName repeatableParamParserType() {
    return generatedClass.nestedClass("RepeatableParamParser");
  }

  public ClassName optionType() {
    return generatedClass.nestedClass("Option");
  }

  public ClassName parserStateType() {
    return generatedClass.nestedClass("ParserState");
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

  public Optional<ClassName> helpRequestedType() {
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

  public List<Parameter> parameters() {
    return parameters;
  }

  public boolean isHelpParameterEnabled() {
    return helpParameterEnabled;
  }

  public String programName() {
    return programName;
  }
}
