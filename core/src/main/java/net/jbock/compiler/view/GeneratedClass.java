package net.jbock.compiler.view;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import net.jbock.compiler.GeneratedTypes;
import net.jbock.compiler.parameter.NamedOption;
import net.jbock.convert.ConvertedParameter;
import net.jbock.qualifier.AllParameters;
import net.jbock.qualifier.AnyDescriptionKeys;
import net.jbock.qualifier.ExitHookField;
import net.jbock.qualifier.GeneratedType;
import net.jbock.qualifier.NamedOptions;
import net.jbock.qualifier.SourceElement;

import javax.inject.Inject;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.ParameterSpec.builder;
import static com.squareup.javapoet.TypeName.INT;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Constants.LIST_OF_STRING;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Constants.STRING_TO_STRING_MAP;
import static net.jbock.compiler.Constants.mapOf;

/**
 * Generates the *_Parser class.
 */
public final class GeneratedClass {

  private static final int DEFAULT_WRAP_AFTER = 80;

  private static final String PROJECT_URL = "https://github.com/h908714124/jbock";

  static final int CONTINUATION_INDENT_USAGE = 8;
  static final String OPTIONS_BY_NAME = "OPTIONS_BY_NAME";
  static final FieldSpec SUSPICIOUS_PATTERN = FieldSpec.builder(Pattern.class, "SUSPICIOUS")
      .initializer("$T.compile($S)", Pattern.class, "-[a-zA-Z0-9]+|--[a-zA-Z0-9-]+")
      .addModifiers(PRIVATE, STATIC, FINAL)
      .build();

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
  private final ExitHookField exitHookField;
  private final ParseOrExitMethod parseOrExitMethod;
  private final OptionParsersMethod optionParsersMethod;
  private final PrintOptionMethod printOptionMethod;
  private final MakeLinesMethod makeLinesMethod;
  private final ParseResultWithRest parseResultWithRest;
  private final ReadOptionArgumentMethod readOptionArgumentMethod;
  private final UsageMethod usageMethod;

  private final FieldSpec err = FieldSpec.builder(PrintStream.class, "err", PRIVATE)
      .initializer("$T.err", System.class).build();

  private final FieldSpec terminalWidth = FieldSpec.builder(INT, "terminalWidth", PRIVATE)
      .initializer("$L", DEFAULT_WRAP_AFTER).build();

  private final FieldSpec messages = FieldSpec.builder(STRING_TO_STRING_MAP, "messages", PRIVATE)
      .initializer("$T.emptyMap()", Collections.class).build();

  private final FieldSpec programName;

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
      ExitHookField exitHookField,
      ParseOrExitMethod parseOrExitMethod,
      OptionParsersMethod optionParsersMethod,
      PrintOptionMethod printOptionMethod,
      MakeLinesMethod makeLinesMethod,
      ParseResultWithRest parseResultWithRest,
      ReadOptionArgumentMethod readOptionArgumentMethod,
      UsageMethod usageMethod) {
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
    this.programName = FieldSpec.builder(STRING, "programName", PRIVATE, FINAL)
        .initializer("$S", sourceElement.programName()).build();
    this.exitHookField = exitHookField;
    this.parseOrExitMethod = parseOrExitMethod;
    this.optionParsersMethod = optionParsersMethod;
    this.printOptionMethod = printOptionMethod;
    this.makeLinesMethod = makeLinesMethod;
    this.parseResultWithRest = parseResultWithRest;
    this.readOptionArgumentMethod = readOptionArgumentMethod;
    this.usageMethod = usageMethod;
  }

  public TypeSpec define() {
    TypeSpec.Builder spec = TypeSpec.classBuilder(generatedType.type())
        .addMethod(parseMethod.define())
        .addMethod(parseOrExitMethod.define())
        .addMethod(withTerminalWidthMethod())
        .addMethod(withMessagesMethod())
        .addMethod(withExitHookMethod())
        .addMethod(withErrorStreamMethod())
        .addMethod(printOnlineHelpMethod.define())
        .addMethod(printOptionMethod.define())
        .addMethod(printTokensMethod())
        .addMethod(makeLinesMethod.define())
        .addMethod(usageMethod.define());
    if (!namedOptions.isEmpty()) {
      spec.addMethod(readOptionArgumentMethod.define());
      spec.addMethod(optionsByNameMethod());
      spec.addMethod(optionParsersMethod.define());
    }
    if (allParameters.anyRequired()) {
      spec.addMethod(missingRequiredMethod());
    }

    spec.addField(err);
    spec.addField(programName);
    spec.addField(terminalWidth);
    spec.addField(exitHookField.get());
    if (anyDescriptionKeys.anyDescriptionKeysAtAll()) {
      spec.addField(messages);
    }

    if (!namedOptions.isEmpty()) {
      spec.addField(FieldSpec.builder(mapOf(STRING, generatedType.optionType()), OPTIONS_BY_NAME)
          .initializer("optionsByName()")
          .addModifiers(PRIVATE, STATIC, FINAL)
          .build());
    }

    spec.addField(SUSPICIOUS_PATTERN);

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

  private MethodSpec printTokensMethod() {
    ParameterSpec continuationIndent = builder(STRING, "continuationIndent").build();
    ParameterSpec tokens = builder(LIST_OF_STRING, "tokens").build();
    ParameterSpec lines = builder(LIST_OF_STRING, "lines").build();
    ParameterSpec line = builder(STRING, "line").build();
    CodeBlock.Builder code = CodeBlock.builder();
    code.addStatement("$T $N = makeLines($N, $N)", lines.type, lines, continuationIndent, tokens);
    code.add("for ($T $N : $N)\n", STRING, line, lines).indent()
        .addStatement("$N.println($N)", err, line)
        .unindent();
    return methodBuilder("printTokens")
        .addModifiers(PRIVATE)
        .addCode(code.build())
        .addParameter(continuationIndent)
        .addParameter(tokens)
        .build();
  }


  private MethodSpec withTerminalWidthMethod() {
    ParameterSpec width = builder(terminalWidth.type, "width").build();
    return methodBuilder("withTerminalWidth")
        .addParameter(width)
        .addStatement("this.$1N = $2N == 0 ? this.$1N : $2N", terminalWidth, width)
        .addStatement("return this")
        .returns(generatedType.type())
        .addModifiers(sourceElement.accessModifiers())
        .build();
  }

  private MethodSpec withExitHookMethod() {
    FieldSpec exitHook = exitHookField.get();
    ParameterSpec param = builder(exitHook.type, exitHook.name).build();
    return methodBuilder("withExitHook")
        .addParameter(param)
        .addStatement("this.$N = $N", exitHook, param)
        .addStatement("return this")
        .returns(generatedType.type())
        .addModifiers(sourceElement.accessModifiers())
        .build();
  }

  private MethodSpec withMessagesMethod() {
    ParameterSpec resourceBundleParam = builder(messages.type, "map").build();
    MethodSpec.Builder spec = methodBuilder("withMessages");
    spec.addParameter(resourceBundleParam);
    if (anyDescriptionKeys.anyDescriptionKeysAtAll()) {
      spec.addStatement("this.$N = $N", messages, resourceBundleParam);
    } else {
      spec.addComment("no keys defined");
    }
    spec.addStatement("return this");
    return spec.returns(generatedType.type())
        .addModifiers(sourceElement.accessModifiers())
        .build();
  }

  private MethodSpec withErrorStreamMethod() {
    ParameterSpec param = builder(err.type, err.name).build();
    return methodBuilder("withErrorStream")
        .addParameter(param)
        .addStatement("this.$N = $N", err, param)
        .addStatement("return this")
        .returns(generatedType.type())
        .addModifiers(sourceElement.accessModifiers())
        .build();
  }


  private CodeBlock javadoc() {
    String version = getClass().getPackage().getImplementationVersion();
    return CodeBlock.builder()
        .add("<h3>Generated by <a href=$S>jbock $L</a></h3>\n", PROJECT_URL, version)
        .add("<p>Use the default constructor to obtain an instance of this parser.</p>\n")
        .build();
  }

  private MethodSpec optionsByNameMethod() {
    ParameterSpec result = builder(mapOf(STRING, generatedType.optionType()), "result").build();
    CodeBlock.Builder code = CodeBlock.builder();
    long mapSize = namedOptions.stream()
        .map(ConvertedParameter::parameter)
        .map(NamedOption::names)
        .map(List::size)
        .mapToLong(i -> i)
        .sum();
    code.addStatement("$T $N = new $T<>($L)", result.type, result, HashMap.class, mapSize);
    for (ConvertedParameter<NamedOption> namedOption : namedOptions.options()) {
      for (String dashedName : namedOption.parameter().names()) {
        code.addStatement("$N.put($S, $T.$L)", result, dashedName, generatedType.optionType(),
            namedOption.enumConstant());
      }
    }
    code.addStatement("return $N", result);

    return MethodSpec.methodBuilder("optionsByName").returns(result.type)
        .addCode(code.build())
        .addModifiers(PRIVATE, STATIC)
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
