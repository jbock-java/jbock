package net.jbock.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import net.jbock.qualifier.SourceElement;

import java.util.Optional;
import java.util.function.Function;

public class GeneratedTypes {

  private final ClassName generatedClass;

  // whether "--help" is a special token
  private final ParserFlavour flavour;

  // the annotated class
  private final SourceElement sourceElement;

  public GeneratedTypes(ClassName generatedClass, ParserFlavour flavour, SourceElement sourceElement) {
    this.generatedClass = generatedClass;
    this.flavour = flavour;
    this.sourceElement = sourceElement;
  }

  public TypeName sourceType() {
    return TypeName.get(sourceElement.element().asType());
  }

  public TypeName parseSuccessType() {
    Optional<TypeName> className = parseResultWithRestType().map(Function.identity());
    return className.orElse(sourceType());
  }

  public Optional<ClassName> parseResultWithRestType() {
    if (!flavour.isSuperCommand()) {
      return Optional.empty();
    }
    return Optional.of(generatedClass.nestedClass(sourceElement.element().getSimpleName() + "WithRest"));
  }

  public ClassName optionParserType() {
    return generatedClass.nestedClass("OptionParser");
  }

  public ClassName repeatableOptionParserType() {
    return generatedClass.nestedClass("RepeatableOptionParser");
  }

  public ClassName flagParserType() {
    return generatedClass.nestedClass("FlagParser");
  }

  public ClassName regularOptionParserType() {
    return generatedClass.nestedClass("RegularOptionParser");
  }

  public ClassName optionType() {
    return generatedClass.nestedClass("Option");
  }

  public ClassName statefulParserType() {
    return generatedClass.nestedClass("StatefulParser");
  }

  public ClassName implType() {
    return generatedClass.nestedClass(sourceElement.element().getSimpleName() + "Impl");
  }

  public ClassName parseResultType() {
    return generatedClass.nestedClass("ParseResult");
  }

  public ClassName parsingSuccessWrapperType() {
    return generatedClass.nestedClass("ParsingSuccess");
  }

  public ClassName parsingFailedType() {
    return generatedClass.nestedClass("ParsingFailed");
  }

  public Optional<ClassName> helpRequestedType() {
    boolean helpParameterEnabled = flavour.helpEnabled(sourceElement.element());
    return helpParameterEnabled ? Optional.of(generatedClass.nestedClass("HelpRequested")) : Optional.empty();
  }
}
