package net.jbock.context;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import net.jbock.either.Either;
import net.jbock.processor.SourceElement;
import net.jbock.util.HelpRequested;
import net.jbock.util.NotSuccess;
import net.jbock.util.SuperResult;
import net.jbock.util.SyntaxError;

import javax.inject.Inject;
import java.util.Optional;

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
    return superResultType().orElse(sourceElement.typeName());
  }

  public Optional<TypeName> superResultType() {
    if (!sourceElement.isSuperCommand()) {
      return Optional.empty();
    }
    ParameterizedTypeName type = ParameterizedTypeName.get(
        ClassName.get(SuperResult.class),
        sourceElement.typeName());
    return Optional.of(type);
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

  public TypeName parseResultType() {
    return ParameterizedTypeName.get(
        ClassName.get(Either.class),
        ClassName.get(NotSuccess.class),
        parseSuccessType());
  }

  public ClassName parsingFailedType() {
    return ClassName.get(SyntaxError.class);
  }

  public Optional<ClassName> helpRequestedType() {
    return sourceElement.helpEnabled() ?
        Optional.of(ClassName.get(HelpRequested.class)) :
        Optional.empty();
  }
}
