package net.jbock.context;

import com.squareup.javapoet.TypeSpec;
import net.jbock.processor.SourceElement;

import javax.inject.Inject;
import javax.lang.model.element.Modifier;

import static javax.lang.model.element.Modifier.FINAL;

/**
 * Generates the *_Parser class.
 */
@ContextScope
public final class GeneratedClass {

  static final int CONTINUATION_INDENT_USAGE = 8;

  private final ParseMethod parseMethod;
  private final Impl impl;
  private final OptionParser optionParser;
  private final OptionEnum optionEnum;
  private final StatefulParser statefulParser;
  private final SourceElement sourceElement;
  private final NamedOptions namedOptions;
  private final AnyDescriptionKeys anyDescriptionKeys;
  private final PrintUsageDocumentationMethod printUsageDocumentationMethod;
  private final CommonFields commonFields;
  private final ParseOrExitMethod parseOrExitMethod;
  private final PrintItemDocumentationMethod printItemDocumentationMethod;
  private final MakeLinesMethod makeLinesMethod;
  private final ReadOptionArgumentMethod readOptionArgumentMethod;
  private final UsageMethod usageMethod;
  private final Withers withers;
  private final GeneratedAnnotation generatedAnnotation;
  private final ReadOptionNameMethod readOptionNameMethod;
  private final ConvEx convEx;
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
      AnyDescriptionKeys anyDescriptionKeys,
      PrintUsageDocumentationMethod printUsageDocumentationMethod,
      CommonFields commonFields,
      ParseOrExitMethod parseOrExitMethod,
      PrintItemDocumentationMethod printItemDocumentationMethod,
      MakeLinesMethod makeLinesMethod,
      ReadOptionArgumentMethod readOptionArgumentMethod,
      UsageMethod usageMethod,
      Withers withers,
      GeneratedAnnotation generatedAnnotation,
      ReadOptionNameMethod readOptionNameMethod,
      ConvEx convEx,
      CreateModelMethod createModelMethod) {
    this.parseMethod = parseMethod;
    this.sourceElement = sourceElement;
    this.impl = impl;
    this.optionParser = optionParser;
    this.optionEnum = optionEnum;
    this.statefulParser = statefulParser;
    this.namedOptions = namedOptions;
    this.anyDescriptionKeys = anyDescriptionKeys;
    this.printUsageDocumentationMethod = printUsageDocumentationMethod;
    this.commonFields = commonFields;
    this.parseOrExitMethod = parseOrExitMethod;
    this.printItemDocumentationMethod = printItemDocumentationMethod;
    this.makeLinesMethod = makeLinesMethod;
    this.readOptionArgumentMethod = readOptionArgumentMethod;
    this.usageMethod = usageMethod;
    this.withers = withers;
    this.generatedAnnotation = generatedAnnotation;
    this.readOptionNameMethod = readOptionNameMethod;
    this.convEx = convEx;
    this.createModelMethod = createModelMethod;
  }

  public TypeSpec define() {
    TypeSpec.Builder spec = TypeSpec.classBuilder(sourceElement.generatedClass())
        .addMethod(parseMethod.get())
        .addMethod(parseOrExitMethod.get())
        .addMethod(withers.withTerminalWidthMethod())
        .addMethod(withers.withMessagesMethod())
        .addMethod(withers.withErrorStreamMethod())
        .addMethod(withers.withExitHookMethod())
        .addMethod(printUsageDocumentationMethod.get())
        .addMethod(printItemDocumentationMethod.get())
        .addMethod(makeLinesMethod.get())
        .addMethod(createModelMethod.get())
        .addMethod(usageMethod.get());
    if (!namedOptions.isEmpty()) {
      spec.addMethod(readOptionNameMethod.get());
      if (namedOptions.anyRepeatable() || namedOptions.anyRegular()) {
        spec.addMethod(readOptionArgumentMethod.get());
      }
    }

    spec.addField(commonFields.err());
    spec.addField(commonFields.terminalWidth());
    if (anyDescriptionKeys.anyDescriptionKeysAtAll()) {
      spec.addField(commonFields.messages());
    }
    spec.addField(commonFields.exitHook());

    spec.addType(statefulParser.get())
        .addType(optionEnum.define())
        .addType(impl.define())
        .addType(convEx.define())
        .addTypes(optionParser.define());

    return spec.addModifiers(FINAL)
        .addOriginatingElement(sourceElement.element())
        .addModifiers(sourceElement.accessModifiers().toArray(new Modifier[0]))
        .addAnnotation(generatedAnnotation.get()).build();
  }
}
