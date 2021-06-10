package net.jbock.context;

import com.squareup.javapoet.TypeSpec;
import net.jbock.processor.SourceElement;

import javax.inject.Inject;
import javax.lang.model.element.Modifier;

import static javax.lang.model.element.Modifier.FINAL;

/**
 * Generates the *Parser class.
 */
@ContextScope
public final class GeneratedClass {

  private final ParseMethod parseMethod;
  private final Impl impl;
  private final OptionParser optionParser;
  private final OptionEnum optionEnum;
  private final StatefulParser statefulParser;
  private final SourceElement sourceElement;
  private final NamedOptions namedOptions;
  private final ParseOrExitMethod parseOrExitMethod;
  private final ReadOptionArgumentMethod readOptionArgumentMethod;
  private final GeneratedAnnotation generatedAnnotation;
  private final ReadOptionNameMethod readOptionNameMethod;
  private final CreateModelMethod createModelMethod;

  @Inject
  GeneratedClass(
      ParseMethod parseMethod,
      SourceElement sourceElement,
      Impl impl,
      OptionParser optionParser,
      OptionEnum optionEnum,
      StatefulParser statefulParser,
      NamedOptions namedOptions,
      ParseOrExitMethod parseOrExitMethod,
      ReadOptionArgumentMethod readOptionArgumentMethod,
      GeneratedAnnotation generatedAnnotation,
      ReadOptionNameMethod readOptionNameMethod,
      CreateModelMethod createModelMethod) {
    this.parseMethod = parseMethod;
    this.sourceElement = sourceElement;
    this.impl = impl;
    this.optionParser = optionParser;
    this.optionEnum = optionEnum;
    this.statefulParser = statefulParser;
    this.namedOptions = namedOptions;
    this.parseOrExitMethod = parseOrExitMethod;
    this.readOptionArgumentMethod = readOptionArgumentMethod;
    this.generatedAnnotation = generatedAnnotation;
    this.readOptionNameMethod = readOptionNameMethod;
    this.createModelMethod = createModelMethod;
  }

  public TypeSpec define() {
    TypeSpec.Builder spec = TypeSpec.classBuilder(sourceElement.generatedClass())
        .addMethod(parseMethod.get())
        .addMethod(parseOrExitMethod.get());
    if (!namedOptions.isEmpty()) {
      spec.addMethod(readOptionNameMethod.get());
      if (namedOptions.anyRepeatable() || namedOptions.anyRegular()) {
        spec.addMethod(readOptionArgumentMethod.get());
      }
    }

    spec.addType(statefulParser.get());
    if (!namedOptions.isEmpty()) {
      spec.addType(optionEnum.define());
      spec.addTypes(optionParser.define());
    }
    spec.addType(impl.define());

    spec.addMethod(createModelMethod.get());

    return spec.addModifiers(FINAL)
        .addOriginatingElement(sourceElement.element())
        .addModifiers(sourceElement.accessModifiers().toArray(new Modifier[0]))
        .addAnnotation(generatedAnnotation.get()).build();
  }
}
