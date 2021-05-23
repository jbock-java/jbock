package net.jbock.compiler.view;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.compiler.GeneratedTypes;
import net.jbock.compiler.color.Styler;
import net.jbock.compiler.parameter.NamedOption;
import net.jbock.compiler.parameter.PositionalParameter;
import net.jbock.convert.ConvertedParameter;
import net.jbock.qualifier.CommonFields;
import net.jbock.qualifier.NamedOptions;
import net.jbock.qualifier.PositionalParameters;
import net.jbock.qualifier.SourceElement;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Constants.STRING_ARRAY;

public class BuildMethod {

  private final GeneratedTypes generatedTypes;
  private final SourceElement sourceElement;
  private final NamedOptions namedOptions;
  private final PositionalParameters positionalParameters;
  private final CommonFields commonFields;
  private final MissingRequiredMethod missingRequiredMethod;
  private final Styler styler;

  @Inject
  BuildMethod(
      GeneratedTypes generatedTypes,
      SourceElement sourceElement,
      NamedOptions namedOptions,
      PositionalParameters positionalParameters,
      CommonFields commonFields,
      MissingRequiredMethod missingRequiredMethod,
      Styler styler) {
    this.generatedTypes = generatedTypes;
    this.sourceElement = sourceElement;
    this.namedOptions = namedOptions;
    this.positionalParameters = positionalParameters;
    this.commonFields = commonFields;
    this.missingRequiredMethod = missingRequiredMethod;
    this.styler = styler;
  }

  MethodSpec get() {
    CodeBlock constructorArguments = getConstructorArguments();
    MethodSpec.Builder spec = MethodSpec.methodBuilder("build");
    generatedTypes.parseResultWithRestType().ifPresentOrElse(parseResultWithRestType -> {
          ParameterSpec result = ParameterSpec.builder(sourceElement.typeName(), "result").build();
          ParameterSpec restArgs = ParameterSpec.builder(sourceElement.typeName(), "restArgs").build();
          spec.addStatement("$T $N = new $T($L)", result.type, result, generatedTypes.implType(),
              constructorArguments);
          spec.addStatement("$T $N = $N.toArray(new $T[0])", STRING_ARRAY, restArgs,
              commonFields.rest(), STRING);
          spec.addStatement("return new $T($N, $N)", parseResultWithRestType,
              result, restArgs);
        },
        () -> spec.addStatement("return new $T($L)", generatedTypes.implType(), constructorArguments));
    return spec.returns(generatedTypes.parseSuccessType())
        .build();
  }

  private CodeBlock getConstructorArguments() {
    List<CodeBlock> code = new ArrayList<>();
    for (ConvertedParameter<NamedOption> option : namedOptions.options()) {
      CodeBlock streamExpression = streamExpressionOption(option);
      code.add(extractExpressionOption(streamExpression, option));
    }
    for (ConvertedParameter<PositionalParameter> param : positionalParameters.regular()) {
      CodeBlock streamExpression = streamExpressionParameter(param);
      code.add(extractExpressionParameter(streamExpression, param));
    }
    positionalParameters.repeatable()
        .map(param -> {
          List<CodeBlock> block = new ArrayList<>();
          block.add(CodeBlock.of("$N.stream()", commonFields.rest()));
          block.add(param.mapExpr());
          block.add(CodeBlock.of(".collect($T.toList())", Collectors.class));
          return joinIndent(block);
        })
        .ifPresent(code::add);
    return joinCodeBlocks(code);
  }

  private CodeBlock extractExpressionOption(
      CodeBlock streamExpression,
      ConvertedParameter<NamedOption> option) {
    List<CodeBlock> code = new ArrayList<>();
    code.add(streamExpression);
    code.add(option.mapExpr());
    code.addAll(tailExpressionOption(option));
    return joinIndent(code);
  }

  private CodeBlock extractExpressionParameter(
      CodeBlock streamExpression,
      ConvertedParameter<PositionalParameter> param) {
    List<CodeBlock> code = new ArrayList<>();
    code.add(streamExpression);
    code.add(param.mapExpr());
    code.addAll(tailExpressionParameter(param));
    return joinIndent(code);
  }

  private CodeBlock streamExpressionOption(ConvertedParameter<NamedOption> option) {
    return CodeBlock.builder().add(
        "$N.get($T.$N).stream()", commonFields.optionParsers(),
        sourceElement.optionType(), option.enumConstant()).build();
  }

  private CodeBlock streamExpressionParameter(ConvertedParameter<PositionalParameter> parameter) {
    return CodeBlock.builder().add(
        "$T.ofNullable($N[$L])", Optional.class, commonFields.params(),
        parameter.parameter().position()).build();
  }

  private List<CodeBlock> tailExpressionOption(ConvertedParameter<NamedOption> parameter) {
    List<String> optionNames = parameter.parameter().names().stream()
        .map(styler::yellow)
        .collect(Collectors.toList());
    String paramLabel = parameter.paramLabel();
    switch (parameter.skew()) {
      case REQUIRED:
        String name = "option: " + paramLabel + " (" + String.join(", ", optionNames) + ")";
        return Arrays.asList(
            CodeBlock.of(".findAny()"),
            CodeBlock.of(".orElseThrow(() -> $N($S))",
                missingRequiredMethod.get(), name));
      case OPTIONAL:
        return singletonList(CodeBlock.of(".findAny()"));
      case REPEATABLE:
        return singletonList(CodeBlock.of(".collect($T.toList())", Collectors.class));
      case FLAG:
        return singletonList(CodeBlock.of(".findAny().isPresent()"));
      default:
        throw new UnsupportedOperationException("unknown skew: " + parameter.skew());
    }
  }

  private List<CodeBlock> tailExpressionParameter(ConvertedParameter<PositionalParameter> parameter) {
    String paramLabel = parameter.paramLabel();
    switch (parameter.skew()) {
      case REQUIRED:
        return singletonList(CodeBlock.of(".orElseThrow(() -> $N($S))",
            missingRequiredMethod.get(), "parameter: " + styler.yellow(paramLabel)));
      case OPTIONAL:
        return emptyList();
      case REPEATABLE:
        return singletonList(CodeBlock.of(".collect($T.toList())", Collectors.class));
      case FLAG:
        return singletonList(CodeBlock.of(".findAny().isPresent()"));
      default:
        throw new UnsupportedOperationException("unknown skew: " + parameter.skew());
    }
  }

  private CodeBlock joinCodeBlocks(List<CodeBlock> code) {
    CodeBlock.Builder args = CodeBlock.builder().add("\n");
    for (int i = 0; i < code.size(); i++) {
      if (i != 0) {
        args.add(",\n");
      }
      args.add(code.get(i));
    }
    return args.build();
  }

  private CodeBlock joinIndent(List<CodeBlock> code) {
    code = code.stream().filter(c -> !c.isEmpty()).collect(Collectors.toList());
    if (code.isEmpty()) {
      return CodeBlock.builder().build();
    }
    if (code.size() == 1) {
      return code.get(0);
    }
    CodeBlock.Builder result = CodeBlock.builder();
    for (int i = 0; i < code.size(); i++) {
      if (i == 0) {
        result.add(code.get(i));
      } else if (i == 1) {
        result.add("\n");
        result.indent();
        result.add(code.get(i));
      } else {
        result.add("\n");
        result.add(code.get(i));
      }
    }
    return result.unindent().build();
  }
}
