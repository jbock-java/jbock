package net.jbock.context;

import com.squareup.javapoet.TypeSpec;
import net.jbock.convert.Mapped;
import net.jbock.parameter.AbstractItem;
import net.jbock.processor.SourceElement;

import javax.inject.Inject;
import javax.lang.model.element.Modifier;

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
  private final AllItems allItems;
  private final ParseOrExitMethod parseOrExitMethod;
  private final GeneratedAnnotation generatedAnnotation;
  private final CreateModelMethod createModelMethod;
  private final MultilineConverter multilineConverter;

  @Inject
  GeneratedClass(
      ParseMethod parseMethod,
      SourceElement sourceElement,
      Impl impl,
      OptionParser optionParser,
      OptionEnum optionEnum,
      StatefulParser statefulParser,
      NamedOptions namedOptions,
      AllItems allItems,
      ParseOrExitMethod parseOrExitMethod,
      GeneratedAnnotation generatedAnnotation,
      CreateModelMethod createModelMethod,
      MultilineConverter multilineConverter) {
    this.parseMethod = parseMethod;
    this.sourceElement = sourceElement;
    this.impl = impl;
    this.optionParser = optionParser;
    this.optionEnum = optionEnum;
    this.statefulParser = statefulParser;
    this.namedOptions = namedOptions;
    this.allItems = allItems;
    this.parseOrExitMethod = parseOrExitMethod;
    this.generatedAnnotation = generatedAnnotation;
    this.createModelMethod = createModelMethod;
    this.multilineConverter = multilineConverter;
  }

  public TypeSpec define() {
    TypeSpec.Builder spec = TypeSpec.classBuilder(sourceElement.generatedClass())
        .addMethod(parseMethod.get())
        .addMethod(parseOrExitMethod.define());

    spec.addType(statefulParser.define());
    if (!namedOptions.isEmpty()) {
      spec.addType(optionEnum.define());
      spec.addTypes(optionParser.define());
    }
    spec.addType(impl.define());

    for (Mapped<? extends AbstractItem> item : allItems.items()) {
      if (item.mapExpr().multiline()) {
        spec.addType(multilineConverter.define(item));
      }
    }

    spec.addMethod(createModelMethod.get());

    return spec.addOriginatingElement(sourceElement.element())
        .addModifiers(sourceElement.accessModifiers().toArray(new Modifier[0]))
        .addAnnotation(generatedAnnotation.define()).build();
  }
}
