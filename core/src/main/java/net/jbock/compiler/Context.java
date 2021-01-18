package net.jbock.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import net.jbock.Command;
import net.jbock.compiler.parameter.Parameter;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.jbock.compiler.Constants.ALLOWED_MODIFIERS;

public final class Context {

  // the annotated class
  private final TypeElement sourceElement;

  // the class that will be generated
  private final ClassName generatedClass;

  // the abstract methods in the annotated class
  private final List<Parameter> parameters;

  private final List<Parameter> params;

  private final List<Parameter> options;

  // whether "--help" is a special token
  private final boolean helpParameterEnabled;

  // program name from attribute
  private final String programName;

  private final ClassName optionType;

  Context(TypeElement sourceElement, ClassName generatedClass, ClassName optionType, List<Parameter> parameters) {
    this.sourceElement = sourceElement;
    this.generatedClass = generatedClass;
    this.parameters = parameters;
    this.params = parameters.stream().filter(Parameter::isPositional).collect(Collectors.toList());
    this.options = parameters.stream().filter(parameter -> !parameter.isPositional()).collect(Collectors.toList());
    this.helpParameterEnabled = !sourceElement.getAnnotation(Command.class).helpDisabled();
    this.programName = programName(sourceElement);
    this.optionType = optionType;
  }

  private static String programName(TypeElement sourceType) {
    if (!sourceType.getAnnotation(Command.class).value().isEmpty()) {
      return sourceType.getAnnotation(Command.class).value();
    }
    return EnumName.create(sourceType.getSimpleName().toString()).snake('-');
  }

  public ClassName optionParserType() {
    return generatedClass.nestedClass("OptionParser");
  }

  public ClassName repeatableParamParserType() {
    return generatedClass.nestedClass("ParamParser");
  }

  public ClassName flagParserType() {
    return generatedClass.nestedClass("FlagParser");
  }

  public ClassName regularOptionParserType() {
    return generatedClass.nestedClass("RegularOptionParser");
  }

  public ClassName regularParamParserType() {
    return generatedClass.nestedClass("RegularParamParser");
  }

  public ClassName optionType() {
    return optionType;
  }

  public ClassName parserStateType() {
    return generatedClass.nestedClass("ParserState");
  }

  public ClassName implType() {
    return generatedClass.nestedClass(sourceElement.getSimpleName() + "Impl");
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

  public TypeName sourceType() {
    return TypeName.get(sourceElement.asType());
  }

  public Modifier[] getAccessModifiers() {
    return sourceElement.getModifiers().stream().filter(ALLOWED_MODIFIERS::contains).toArray(Modifier[]::new);
  }

  public ClassName generatedClass() {
    return generatedClass;
  }

  public List<Parameter> parameters() {
    return parameters;
  }

  public List<Parameter> params() {
    return params;
  }

  public List<Parameter> options() {
    return options;
  }

  public boolean isHelpParameterEnabled() {
    return helpParameterEnabled;
  }

  public String programName() {
    return programName;
  }
}
