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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Constants.STRING_ARRAY;

public class BuildMethod {

  private final GeneratedTypes generatedTypes;
  private final SourceElement sourceElement;
  private final NamedOptions namedOptions;
  private final PositionalParameters positionalParameters;
  private final CommonFields commonFields;
  private final Styler styler;

  @Inject
  BuildMethod(
      GeneratedTypes generatedTypes,
      SourceElement sourceElement,
      NamedOptions namedOptions,
      PositionalParameters positionalParameters,
      CommonFields commonFields,
      Styler styler) {
    this.generatedTypes = generatedTypes;
    this.sourceElement = sourceElement;
    this.namedOptions = namedOptions;
    this.positionalParameters = positionalParameters;
    this.commonFields = commonFields;
    this.styler = styler;
  }

  MethodSpec get() {
    CodeBlock constructorArguments = getConstructorArguments();
    MethodSpec.Builder spec = MethodSpec.methodBuilder("build");
    for (ConvertedParameter<NamedOption> option : namedOptions.options()) {
      CodeBlock streamExpression = streamExpressionOption(option);
      spec.addStatement("$T $N = $L", option.implConstructorParam().type, option.implConstructorParam(),
          extractExpressionOption(streamExpression, option));
    }
    for (ConvertedParameter<PositionalParameter> param : positionalParameters.regular()) {
      CodeBlock streamExpression = streamExpressionParameter(param);
      spec.addStatement("$T $N = $L", param.implConstructorParam().type, param.implConstructorParam(),
          extractExpressionParameter(streamExpression, param));
    }
    positionalParameters.repeatable().ifPresent(param -> {
      List<CodeBlock> block = new ArrayList<>();
      block.add(CodeBlock.of("this.$N.stream()", commonFields.rest()));
      param.mapExpr().ifPresent(block::add);
      block.add(CodeBlock.of(".collect($T.toList())", Collectors.class));
      spec.addStatement("$T $N = $L", param.implConstructorParam().type, param.implConstructorParam(),
          joinByNewline(block));
    });
    generatedTypes.parseResultWithRestType().ifPresentOrElse(parseResultWithRestType -> {
          ParameterSpec result = ParameterSpec.builder(sourceElement.typeName(), "result").build();
          ParameterSpec restArgs = ParameterSpec.builder(sourceElement.typeName(), "restArgs").build();
          spec.addStatement("$T $N = new $T($L)", result.type, result, generatedTypes.implType(),
              constructorArguments);
          spec.addStatement("$T $N = this.$N.toArray(new $T[0])", STRING_ARRAY, restArgs,
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
      code.add(CodeBlock.of("$N", option.implConstructorParam()));
    }
    for (ConvertedParameter<PositionalParameter> param : positionalParameters.regular()) {
      code.add(CodeBlock.of("$N", param.implConstructorParam()));
    }
    positionalParameters.repeatable()
        .map(param -> CodeBlock.of("$N", param.implConstructorParam()))
        .ifPresent(code::add);
    return joinByComma(code);
  }

  private CodeBlock extractExpressionOption(
      CodeBlock streamExpression,
      ConvertedParameter<NamedOption> option) {
    List<CodeBlock> code = new ArrayList<>();
    code.add(streamExpression);
    option.mapExpr().ifPresent(code::add);
    code.addAll(tailExpressionOption(option));
    option.extractExpr().ifPresent(code::add);
    return joinByNewline(code);
  }

  private CodeBlock extractExpressionParameter(
      CodeBlock streamExpression,
      ConvertedParameter<PositionalParameter> param) {
    List<CodeBlock> code = new ArrayList<>();
    code.add(streamExpression);
    param.mapExpr().ifPresent(code::add);
    code.addAll(tailExpressionParameter(param));
    param.extractExpr().ifPresent(code::add);
    return joinByNewline(code);
  }

  private CodeBlock streamExpressionOption(ConvertedParameter<NamedOption> option) {
    return CodeBlock.builder().add(
        "this.$N.get($T.$N).stream()", commonFields.optionParsers(),
        sourceElement.itemType(), option.enumConstant()).build();
  }

  private CodeBlock streamExpressionParameter(ConvertedParameter<PositionalParameter> parameter) {
    return CodeBlock.builder().add(
        "$T.ofNullable(this.$N[$L])", Optional.class, commonFields.params(),
        parameter.parameter().position()).build();
  }

  private List<CodeBlock> tailExpressionOption(ConvertedParameter<NamedOption> parameter) {
    String optionNames = String.join(", ", parameter.parameter().names());
    String paramLabel = parameter.paramLabel();
    switch (parameter.skew()) {
      case REQUIRED:
        String message = "Missing required option: " + paramLabel + " (" + optionNames + ")";
        return List.of(
            CodeBlock.of(".findAny()"),
            CodeBlock.of(".orElseThrow(() -> new $T($S))",
                RuntimeException.class, message));
      case OPTIONAL:
        return List.of(CodeBlock.of(".findAny()"));
      case REPEATABLE:
        return List.of(CodeBlock.of(".collect($T.toList())", Collectors.class));
      case FLAG:
        return List.of(CodeBlock.of(".findAny().isPresent()"));
      default:
        throw new UnsupportedOperationException("unknown skew: " + parameter.skew());
    }
  }

  private List<CodeBlock> tailExpressionParameter(ConvertedParameter<PositionalParameter> parameter) {
    switch (parameter.skew()) {
      case REQUIRED:
        String paramLabel = styler.bold(parameter.paramLabel()).orElse(parameter.paramLabel());
        return List.of(CodeBlock.of(".orElseThrow(() -> new $T($S))",
            RuntimeException.class, "Missing required parameter: " + paramLabel));
      case OPTIONAL:
        return emptyList();
      case REPEATABLE:
        return List.of(CodeBlock.of(".collect($T.toList())", Collectors.class));
      case FLAG:
        return List.of(CodeBlock.of(".findAny().isPresent()"));
      default:
        throw new UnsupportedOperationException("unknown skew: " + parameter.skew());
    }
  }

  private CodeBlock joinByComma(List<CodeBlock> code) {
    CodeBlock.Builder args = CodeBlock.builder();
    for (int i = 0; i < code.size(); i++) {
      if (i != 0) {
        args.add(",$W");
      }
      args.add(code.get(i));
    }
    return args.build();
  }

  private CodeBlock joinByNewline(List<CodeBlock> code) {
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
