package net.jbock.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import dagger.Reusable;
import net.jbock.qualifier.GeneratedType;
import net.jbock.qualifier.SourceElement;

import javax.inject.Inject;
import java.util.Optional;
import java.util.function.Function;

@Reusable
public class GeneratedTypes {

  private final GeneratedType generatedType;
  private final SourceElement sourceElement;

  @Inject
  GeneratedTypes(GeneratedType generatedType, SourceElement sourceElement) {
    this.generatedType = generatedType;
    this.sourceElement = sourceElement;
  }

  public TypeName parseSuccessType() {
    Optional<TypeName> className = parseResultWithRestType().map(Function.identity());
    return className.orElse(sourceElement.typeName());
  }

  public Optional<ClassName> parseResultWithRestType() {
    if (!sourceElement.isSuperCommand()) {
      return Optional.empty();
    }
    return Optional.of(generatedType.type().nestedClass(sourceElement.element().getSimpleName() + "WithRest"));
  }

  public ClassName optionParserType() {
    return generatedType.type().nestedClass("OptionParser");
  }

  public ClassName repeatableOptionParserType() {
    return generatedType.type().nestedClass("RepeatableOptionParser");
  }

  public ClassName flagParserType() {
    return generatedType.type().nestedClass("FlagParser");
  }

  public ClassName regularOptionParserType() {
    return generatedType.type().nestedClass("RegularOptionParser");
  }

  public ClassName optionType() {
    return generatedType.type().nestedClass("Option");
  }

  public ClassName statefulParserType() {
    return generatedType.type().nestedClass("StatefulParser");
  }

  public ClassName implType() {
    return generatedType.type().nestedClass(sourceElement.element().getSimpleName() + "Impl");
  }

  public ClassName parseResultType() {
    return generatedType.type().nestedClass("ParseResult");
  }

  public ClassName parsingSuccessWrapperType() {
    return generatedType.type().nestedClass("ParsingSuccess");
  }

  public ClassName parsingFailedType() {
    return generatedType.type().nestedClass("ParsingFailed");
  }

  public Optional<ClassName> helpRequestedType() {
    return sourceElement.helpEnabled() ?
        Optional.of(generatedType.type().nestedClass("HelpRequested")) :
        Optional.empty();
  }
}
