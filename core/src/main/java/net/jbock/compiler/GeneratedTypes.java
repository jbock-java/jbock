package net.jbock.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import net.jbock.qualifier.SourceElement;
import net.jbock.scope.ContextScope;

import javax.inject.Inject;
import java.util.Optional;
import java.util.function.Function;

@ContextScope
public class GeneratedTypes {

  private final SourceElement sourceElement;
  private final ClassName generatedClass;

  @Inject
  GeneratedTypes(SourceElement sourceElement) {
    this.sourceElement = sourceElement;
    this.generatedClass = sourceElement.generatedClass();
  }

  public TypeName parseSuccessType() {
    Optional<TypeName> className = parseResultWithRestType().map(Function.identity());
    return className.orElse(sourceElement.typeName());
  }

  public Optional<ClassName> parseResultWithRestType() {
    if (!sourceElement.isSuperCommand()) {
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
    return sourceElement.helpEnabled() ?
        Optional.of(generatedClass.nestedClass("HelpRequested")) :
        Optional.empty();
  }
}
