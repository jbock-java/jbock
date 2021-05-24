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
    for (ConvertedParameter<NamedOption> c : namedOptions.options()) {
      ParameterSpec p = c.asParam();
      spec.addStatement("$T $N = $L", p.type, p, convertExpressionOption(c));
    }
    for (ConvertedParameter<PositionalParameter> c : positionalParameters.regular()) {
      ParameterSpec p = c.asParam();
      spec.addStatement("$T $N = $L", p.type, p, convertExpressionRegularParameter(c));
    }
    positionalParameters.repeatable().ifPresent(c -> {
      ParameterSpec p = c.asParam();
      spec.addStatement("$T $N = $L", p.type, p, convertExpressionRepeatableParameter(c));
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
    for (ConvertedParameter<NamedOption> c : namedOptions.options()) {
      code.add(CodeBlock.of("$N", c.asParam()));
    }
    for (ConvertedParameter<PositionalParameter> c : positionalParameters.regular()) {
      code.add(CodeBlock.of("$N", c.asParam()));
    }
    positionalParameters.repeatable()
        .map(c -> CodeBlock.of("$N", c.asParam()))
        .ifPresent(code::add);
    return joinByComma(code);
  }

  private CodeBlock convertExpressionOption(ConvertedParameter<NamedOption> c) {
    List<CodeBlock> code = new ArrayList<>();
    code.add(streamExpressionOption(c));
    c.mapExpr().ifPresent(code::add);
    code.addAll(tailExpressionOption(c));
    c.extractExpr().ifPresent(code::add);
    return joinByNewline(code);
  }

  private CodeBlock convertExpressionRegularParameter(ConvertedParameter<PositionalParameter> c) {
    List<CodeBlock> code = new ArrayList<>();
    code.add(streamExpressionParameter(c));
    c.mapExpr().ifPresent(code::add);
    code.addAll(tailExpressionParameter(c));
    c.extractExpr().ifPresent(code::add);
    return joinByNewline(code);
  }


  private CodeBlock convertExpressionRepeatableParameter(ConvertedParameter<PositionalParameter> c) {
    List<CodeBlock> block = new ArrayList<>();
    block.add(CodeBlock.of("this.$N.stream()", commonFields.rest()));
    c.mapExpr().ifPresent(block::add);
    block.add(CodeBlock.of(".collect($T.toList())", Collectors.class));
    return joinByNewline(block);
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
        throw new UnsupportedOperationException("unexpected skew: " + parameter.skew());
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
      default:
        throw new UnsupportedOperationException("unexpected skew: " + parameter.skew());
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
    boolean indent = false;
    CodeBlock.Builder result = CodeBlock.builder();
    for (int i = 0; i < code.size(); i++) {
      if (i == 0) {
        result.add(code.get(i));
      } else if (i == 1) {
        result.add("\n").indent().add(code.get(i));
        indent = true;
      } else {
        result.add("\n").add(code.get(i));
      }
    }
    if (indent) {
      result.unindent();
    }
    return result.build();
  }
}
