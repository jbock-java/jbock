package net.jbock.compiler.view;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import net.jbock.compiler.GeneratedTypes;
import net.jbock.qualifier.AllParameters;
import net.jbock.qualifier.AnyDescriptionKeys;
import net.jbock.qualifier.CommonFields;
import net.jbock.qualifier.GeneratedType;
import net.jbock.qualifier.NamedOptions;
import net.jbock.qualifier.SourceElement;

import javax.inject.Inject;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.ParameterSpec.builder;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Constants.STRING;

/**
 * Generates the *_Parser class.
 */
public final class GeneratedClass {

  private static final String PROJECT_URL = "https://github.com/h908714124/jbock";

  static final int CONTINUATION_INDENT_USAGE = 8;

  private final AllParameters allParameters;
  private final ParseMethod parseMethod;
  private final Impl impl;
  private final GeneratedTypes generatedTypes;
  private final GeneratedType generatedType;
  private final OptionParser optionParser;
  private final OptionEnum optionEnum;
  private final StatefulParser parserState;
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

  @Inject
  GeneratedClass(
      AllParameters allParameters,
      ParseMethod parseMethod,
      GeneratedType generatedType,
      SourceElement sourceElement,
      Impl impl,
      GeneratedTypes generatedTypes,
      OptionParser optionParser,
      OptionEnum optionEnum,
      StatefulParser parserState,
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
      PrintTokensMethod printTokensMethod) {
    this.parseMethod = parseMethod;
    this.generatedType = generatedType;
    this.sourceElement = sourceElement;
    this.allParameters = allParameters;
    this.impl = impl;
    this.generatedTypes = generatedTypes;
    this.optionParser = optionParser;
    this.optionEnum = optionEnum;
    this.parserState = parserState;
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
  }

  public TypeSpec define() {
    TypeSpec.Builder spec = TypeSpec.classBuilder(generatedType.type())
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
      spec.addMethod(missingRequiredMethod());
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

    spec.addType(parserState.define())
        .addType(optionEnum.define())
        .addType(impl.define())
        .addTypes(optionParser.define())
        .addTypes(parseResult.defineResultTypes());

    generatedTypes.parseResultWithRestType()
        .map(parseResultWithRest::define)
        .ifPresent(spec::addType);

    return spec.addModifiers(FINAL)
        .addModifiers(sourceElement.accessModifiers())
        .addJavadoc(javadoc()).build();
  }

  private CodeBlock javadoc() {
    String version = getClass().getPackage().getImplementationVersion();
    return CodeBlock.builder()
        .add("<h3>Generated by <a href=$S>jbock $L</a></h3>\n", PROJECT_URL, version)
        .add("<p>Use the default constructor to obtain an instance of this parser.</p>\n")
        .build();
  }

  private MethodSpec missingRequiredMethod() {
    ParameterSpec name = builder(STRING, "name").build();
    return methodBuilder("missingRequired")
        .returns(RuntimeException.class)
        .addStatement("return new $T($S + $N)", RuntimeException.class, "Missing required: ", name)
        .addParameter(name)
        .addModifiers(PRIVATE, STATIC)
        .build();
  }
}
