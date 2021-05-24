package net.jbock.compiler.view;

import com.squareup.javapoet.TypeSpec;
import dagger.Reusable;
import net.jbock.compiler.GeneratedTypes;
import net.jbock.qualifier.AnyDescriptionKeys;
import net.jbock.qualifier.CommonFields;
import net.jbock.qualifier.NamedOptions;
import net.jbock.qualifier.SourceElement;

import javax.inject.Inject;
import javax.lang.model.element.Modifier;

import static javax.lang.model.element.Modifier.FINAL;

/**
 * Generates the *_Parser class.
 */
@Reusable
public final class GeneratedClass {

  static final int CONTINUATION_INDENT_USAGE = 8;

  private final ParseMethod parseMethod;
  private final Impl impl;
  private final GeneratedTypes generatedTypes;
  private final OptionParser optionParser;
  private final OptionEnum optionEnum;
  private final StatefulParser statefulParser;
  private final ParseResult parseResult;
  private final SourceElement sourceElement;
  private final NamedOptions namedOptions;
  private final AnyDescriptionKeys anyDescriptionKeys;
  private final PrintUsageDocumentationMethod printUsageDocumentationMethod;
  private final CommonFields commonFields;
  private final ParseOrExitMethod parseOrExitMethod;
  private final PrintItemDocumentationMethod printItemDocumentationMethod;
  private final MakeLinesMethod makeLinesMethod;
  private final ParseResultWithRest parseResultWithRest;
  private final ReadOptionArgumentMethod readOptionArgumentMethod;
  private final UsageMethod usageMethod;
  private final Withers withers;
  private final ClassJavadoc classJavadoc;
  private final ReadOptionNameMethod readOptionNameMethod;

  @Inject
  GeneratedClass(
      ParseMethod parseMethod,
      SourceElement sourceElement,
      Impl impl,
      GeneratedTypes generatedTypes,
      OptionParser optionParser,
      OptionEnum optionEnum,
      StatefulParser statefulParser,
      ParseResult parseResult,
      NamedOptions namedOptions,
      AnyDescriptionKeys anyDescriptionKeys,
      PrintUsageDocumentationMethod printUsageDocumentationMethod,
      CommonFields commonFields,
      ParseOrExitMethod parseOrExitMethod,
      PrintItemDocumentationMethod printItemDocumentationMethod,
      MakeLinesMethod makeLinesMethod,
      ParseResultWithRest parseResultWithRest,
      ReadOptionArgumentMethod readOptionArgumentMethod,
      UsageMethod usageMethod, Withers withers,
      ClassJavadoc classJavadoc,
      ReadOptionNameMethod readOptionNameMethod) {
    this.parseMethod = parseMethod;
    this.sourceElement = sourceElement;
    this.impl = impl;
    this.generatedTypes = generatedTypes;
    this.optionParser = optionParser;
    this.optionEnum = optionEnum;
    this.statefulParser = statefulParser;
    this.parseResult = parseResult;
    this.namedOptions = namedOptions;
    this.anyDescriptionKeys = anyDescriptionKeys;
    this.printUsageDocumentationMethod = printUsageDocumentationMethod;
    this.commonFields = commonFields;
    this.parseOrExitMethod = parseOrExitMethod;
    this.printItemDocumentationMethod = printItemDocumentationMethod;
    this.makeLinesMethod = makeLinesMethod;
    this.parseResultWithRest = parseResultWithRest;
    this.readOptionArgumentMethod = readOptionArgumentMethod;
    this.usageMethod = usageMethod;
    this.withers = withers;
    this.classJavadoc = classJavadoc;
    this.readOptionNameMethod = readOptionNameMethod;
  }

  public TypeSpec define() {
    TypeSpec.Builder spec = TypeSpec.classBuilder(sourceElement.generatedClass())
        .addMethod(parseMethod.get())
        .addMethod(parseOrExitMethod.get())
        .addMethod(withers.withTerminalWidthMethod())
        .addMethod(withers.withMessagesMethod())
        .addMethod(withers.withExitHookMethod())
        .addMethod(withers.withErrorStreamMethod())
        .addMethod(printUsageDocumentationMethod.get())
        .addMethod(printItemDocumentationMethod.get())
        .addMethod(makeLinesMethod.get())
        .addMethod(usageMethod.get());
    if (!namedOptions.isEmpty()) {
      spec.addMethod(readOptionNameMethod.get());
      if (namedOptions.anyRepeatable() || namedOptions.anyRegular()) {
        spec.addMethod(readOptionArgumentMethod.get());
      }
    }

    spec.addField(commonFields.err());
    spec.addField(commonFields.terminalWidth());
    spec.addField(commonFields.exitHook());
    if (anyDescriptionKeys.anyDescriptionKeysAtAll()) {
      spec.addField(commonFields.messages());
    }

    spec.addType(statefulParser.get())
        .addType(optionEnum.define())
        .addType(impl.define())
        .addTypes(optionParser.define())
        .addTypes(parseResult.defineResultTypes());

    generatedTypes.parseResultWithRestType()
        .map(parseResultWithRest::define)
        .ifPresent(spec::addType);

    return spec.addModifiers(FINAL)
        .addOriginatingElement(sourceElement.element())
        .addModifiers(sourceElement.accessModifiers().toArray(new Modifier[0]))
        .addJavadoc(classJavadoc.create()).build();
  }
}
