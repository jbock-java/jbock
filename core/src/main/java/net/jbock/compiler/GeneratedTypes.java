package net.jbock.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.TypeElement;
import java.util.Optional;

public class GeneratedTypes {

  private final ClassName generatedClass;

  // whether "--help" is a special token
  private final ParserFlavour flavour;

  // the annotated class
  private final TypeElement sourceElement;

  public GeneratedTypes(ClassName generatedClass, ParserFlavour flavour, TypeElement sourceElement) {
    this.generatedClass = generatedClass;
    this.flavour = flavour;
    this.sourceElement = sourceElement;
  }

  public TypeName sourceType() {
    return TypeName.get(sourceElement.asType());
  }

  public ClassName generatedClass() {
    return generatedClass;
  }

  public ClassName optionParserType() {
    return generatedClass.nestedClass("OptionParser");
  }

  public ClassName repeatableOptionParserType() {
    return generatedClass.nestedClass("RepeatableOptionParser");
  }

  public ClassName paramParserType() {
    return generatedClass.nestedClass("ParamParser");
  }

  public ClassName repeatableParamParserType() {
    return generatedClass.nestedClass("RepeatableParamParser");
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
    return generatedClass.nestedClass("Option");
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
    boolean helpParameterEnabled = !flavour.helpDisabled(sourceElement);
    return helpParameterEnabled ? Optional.of(generatedClass.nestedClass("HelpRequested")) : Optional.empty();
  }
}
