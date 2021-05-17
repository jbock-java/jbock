package net.jbock.compiler.view;

import com.squareup.javapoet.TypeSpec;
import dagger.Reusable;
import net.jbock.compiler.GeneratedTypes;
import net.jbock.qualifier.AllParameters;
import net.jbock.qualifier.AnyDescriptionKeys;
import net.jbock.qualifier.CommonFields;
import net.jbock.qualifier.NamedOptions;
import net.jbock.qualifier.SourceElement;

import javax.inject.Inject;

import static javax.lang.model.element.Modifier.FINAL;

/**
 * Generates the *_Parser class.
 */
@Reusable
public final class GeneratedClass {

  static final int CONTINUATION_INDENT_USAGE = 8;

  private final AllParameters allParameters;
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
  private final PrintOnlineHelpMethod printOnlineHelpMethod;
  private final CommonFields commonFields;
  private final ParseOrExitMethod parseOrExitMethod;
  private final OptionParsersMethod optionParsersMethod;
  private final PrintOptionMethod printOptionMethod;
  private final MakeLinesMethod makeLinesMethod;
  private final ParseResultWithRest parseResultWithRest;
  private final ReadOptionArgumentMethod readOptionArgumentMethod;
  private final UsageMethod usageMethod;
  private final Withers withers;
  private final OptionsByNameMethod optionsByNameMethod;
  private final PrintTokensMethod printTokensMethod;
  private final ClassJavadoc classJavadoc;
  private final MissingRequiredMethod missingRequiredMethod;

  @Inject
  GeneratedClass(
      AllParameters allParameters,
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
      PrintOnlineHelpMethod printOnlineHelpMethod,
      CommonFields commonFields,
      ParseOrExitMethod parseOrExitMethod,
      OptionParsersMethod optionParsersMethod,
      PrintOptionMethod printOptionMethod,
      MakeLinesMethod makeLinesMethod,
      ParseResultWithRest parseResultWithRest,
      ReadOptionArgumentMethod readOptionArgumentMethod,
      UsageMethod usageMethod, Withers withers,
      OptionsByNameMethod optionsByNameMethod,
      PrintTokensMethod printTokensMethod,
      ClassJavadoc classJavadoc,
      MissingRequiredMethod missingRequiredMethod) {
    this.parseMethod = parseMethod;
    this.sourceElement = sourceElement;
    this.allParameters = allParameters;
    this.impl = impl;
    this.generatedTypes = generatedTypes;
    this.optionParser = optionParser;
    this.optionEnum = optionEnum;
    this.statefulParser = statefulParser;
    this.parseResult = parseResult;
    this.namedOptions = namedOptions;
    this.anyDescriptionKeys = anyDescriptionKeys;
    this.printOnlineHelpMethod = printOnlineHelpMethod;
    this.commonFields = commonFields;
    this.parseOrExitMethod = parseOrExitMethod;
    this.optionParsersMethod = optionParsersMethod;
    this.printOptionMethod = printOptionMethod;
    this.makeLinesMethod = makeLinesMethod;
    this.parseResultWithRest = parseResultWithRest;
    this.readOptionArgumentMethod = readOptionArgumentMethod;
    this.usageMethod = usageMethod;
    this.withers = withers;
    this.optionsByNameMethod = optionsByNameMethod;
    this.printTokensMethod = printTokensMethod;
    this.classJavadoc = classJavadoc;
    this.missingRequiredMethod = missingRequiredMethod;
  }

  public TypeSpec define() {
    TypeSpec.Builder spec = TypeSpec.classBuilder(sourceElement.generatedClass())
        .addMethod(parseMethod.define())
        .addMethod(parseOrExitMethod.define())
        .addMethod(withers.withTerminalWidthMethod())
        .addMethod(withers.withMessagesMethod())
        .addMethod(withers.withExitHookMethod())
        .addMethod(withers.withErrorStreamMethod())
        .addMethod(printOnlineHelpMethod.define())
        .addMethod(printOptionMethod.define())
        .addMethod(printTokensMethod.get())
        .addMethod(makeLinesMethod.define())
        .addMethod(usageMethod.define());
    if (!namedOptions.isEmpty()) {
      spec.addMethod(readOptionArgumentMethod.define());
      spec.addMethod(optionsByNameMethod.define());
      spec.addMethod(optionParsersMethod.define());
    }
    if (allParameters.anyRequired()) {
      spec.addMethod(missingRequiredMethod.method());
    }

    spec.addField(commonFields.err());
    spec.addField(commonFields.programName());
    spec.addField(commonFields.terminalWidth());
    spec.addField(commonFields.exitHook());
    if (anyDescriptionKeys.anyDescriptionKeysAtAll()) {
      spec.addField(commonFields.messages());
    }

    if (!namedOptions.isEmpty()) {
      spec.addField(commonFields.optionsByName());
    }

    spec.addField(commonFields.suspiciousPattern());

    spec.addType(statefulParser.define())
        .addType(optionEnum.define())
        .addType(impl.define())
        .addTypes(optionParser.define())
        .addTypes(parseResult.defineResultTypes());

    generatedTypes.parseResultWithRestType()
        .map(parseResultWithRest::define)
        .ifPresent(spec::addType);

    return spec.addModifiers(FINAL)
        .addOriginatingElement(sourceElement.element())
        .addModifiers(sourceElement.accessModifiers())
        .addJavadoc(classJavadoc.create()).build();
  }
}
