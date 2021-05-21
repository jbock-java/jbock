package net.jbock.compiler.view;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import dagger.Reusable;
import net.jbock.compiler.GeneratedTypes;
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

import static com.squareup.javapoet.TypeName.BOOLEAN;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Constants.STRING_ARRAY;
import static net.jbock.compiler.Constants.STRING_ITERATOR;

/**
 * Defines the inner class StatefulParser
 */
@Reusable
public class StatefulParser extends Cached<TypeSpec> {

  private final StatefulParseMethod statefulParseMethod;
  private final GeneratedTypes generatedTypes;
  private final SourceElement sourceElement;
  private final NamedOptions namedOptions;
  private final PositionalParameters positionalParameters;
  private final CommonFields commonFields;
  private final MissingRequiredMethod missingRequiredMethod;
  private final ReadOptionNameMethod readOptionNameMethod;

  @Inject
  StatefulParser(
      GeneratedTypes generatedTypes,
      StatefulParseMethod statefulParseMethod,
      SourceElement sourceElement,
      NamedOptions namedOptions,
      PositionalParameters positionalParameters,
      CommonFields commonFields,
      MissingRequiredMethod missingRequiredMethod,
      ReadOptionNameMethod readOptionNameMethod) {
    this.generatedTypes = generatedTypes;
    this.statefulParseMethod = statefulParseMethod;
    this.sourceElement = sourceElement;
    this.namedOptions = namedOptions;
    this.positionalParameters = positionalParameters;
    this.commonFields = commonFields;
    this.missingRequiredMethod = missingRequiredMethod;
    this.readOptionNameMethod = readOptionNameMethod;
  }

  @Override
  TypeSpec define() {
    TypeSpec.Builder spec = TypeSpec.classBuilder(generatedTypes.statefulParserType())
        .addModifiers(PRIVATE, STATIC)
        .addMethod(statefulParseMethod.parseMethod());
    spec.addField(commonFields.suspiciousPattern());
    if (!namedOptions.isEmpty()) {
      spec.addMethod(tryParseOptionMethod())
          .addMethod(privateConstructor());
      spec.addField(commonFields.optionNames());
      spec.addField(commonFields.optionParsers());
    }
    if (!positionalParameters.regular().isEmpty()) {
      spec.addField(commonFields.params());
    }
    if (positionalParameters.anyRepeatable() || sourceElement.isSuperCommand()) {
      spec.addField(commonFields.rest());
    }
    spec.addMethod(buildMethod());
    return spec.build();
  }

  private MethodSpec privateConstructor() {
    CodeBlock.Builder code = CodeBlock.builder();
    for (ConvertedParameter<NamedOption> namedOption : namedOptions.options()) {
      for (String dashedName : namedOption.parameter().names()) {
        code.addStatement("$N.put($S, $T.$L)", commonFields.optionNames(), dashedName, sourceElement.optionType(),
            namedOption.enumConstant());
      }
      String enumConstant = namedOption.enumConstant();
      code.addStatement("$N.put($T.$L, new $T($T.$L))",
          commonFields.optionParsers(), sourceElement.optionType(), enumConstant, optionParserType(namedOption),
          sourceElement.optionType(), enumConstant);
    }
    return MethodSpec.constructorBuilder()
        .addCode(code.build())
        .build();
  }


  private ClassName optionParserType(ConvertedParameter<NamedOption> param) {
    if (param.isRepeatable()) {
      return generatedTypes.repeatableOptionParserType();
    }
    if (param.isFlag()) {
      return generatedTypes.flagParserType();
    }
    return generatedTypes.regularOptionParserType();
  }

  private MethodSpec tryParseOptionMethod() {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    return MethodSpec.methodBuilder("tryParseOption")
        .addParameter(token)
        .addParameter(it)
        .addCode(namedOptions.unixClusteringSupported() ?
            tryParseOptionCodeClustering(token, it) :
            tryParseOptionCodeSimple(token, it))
        .returns(BOOLEAN)
        .build();
  }

  private CodeBlock tryParseOptionCodeClustering(ParameterSpec token, ParameterSpec it) {
    ParameterSpec clusterToken = ParameterSpec.builder(STRING, "clusterToken").build();
    ParameterSpec option = ParameterSpec.builder(sourceElement.optionType(), "option").build();
    CodeBlock.Builder code = CodeBlock.builder();
    code.addStatement("$T $N = $N.get($N($N))", sourceElement.optionType(),
        option, commonFields.optionNames(), readOptionNameMethod.get(), token);
    code.add("if ($N == null)\n", option).indent()
        .addStatement("return false")
        .unindent();
    code.addStatement("$T $N = $N", clusterToken.type, clusterToken, token);
    code.beginControlFlow("while ($N.get($N).read($N, $N))", commonFields.optionParsers(), option, clusterToken, it);
    code.addStatement("$1N = '-' + $1N.substring(2, $1N.length())", clusterToken);
    code.addStatement("$N = $N.get($N($N))", option, commonFields.optionNames(), readOptionNameMethod.get(),
        clusterToken);
    code.add("if ($N == null)\n", option).indent()
        .addStatement("throw new $T($S + $N)", RuntimeException.class, "Invalid token: ", token)
        .unindent();
    code.endControlFlow();
    code.addStatement("return true");
    return code.build();
  }

  private CodeBlock tryParseOptionCodeSimple(ParameterSpec token, ParameterSpec it) {
    ParameterSpec option = ParameterSpec.builder(sourceElement.optionType(), "option").build();
    CodeBlock.Builder code = CodeBlock.builder();
    code.addStatement("$T $N = $N.get($N($N))", sourceElement.optionType(), option,
        commonFields.optionNames(), readOptionNameMethod.get(), token);
    code.add("if ($N == null)\n", option).indent()
        .addStatement("return false")
        .unindent();
    code.addStatement("$N.get($N).read($N, $N)", commonFields.optionParsers(), option, token, it)
        .addStatement("return true");
    return code.build();
  }

  private MethodSpec buildMethod() {

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
    MethodSpec.Builder spec = MethodSpec.methodBuilder("build");
    generatedTypes.parseResultWithRestType().ifPresentOrElse(parseResultWithRestType -> {
          ParameterSpec result = ParameterSpec.builder(sourceElement.typeName(), "result").build();
          ParameterSpec restArgs = ParameterSpec.builder(sourceElement.typeName(), "restArgs").build();
          spec.addStatement("$T $N = new $T($L)", result.type, result, generatedTypes.implType(),
              joinCodeBlocks(code));
          spec.addStatement("$T $N = $N.toArray(new $T[0])", STRING_ARRAY, restArgs,
              commonFields.rest(), STRING);
          spec.addStatement("return new $T($N, $N)", parseResultWithRestType,
              result, restArgs);
        },
        () -> spec.addStatement("return new $T($L)", generatedTypes.implType(), joinCodeBlocks(code)));
    return spec.returns(generatedTypes.parseSuccessType())
        .build();
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
    List<String> dashedNames = parameter.parameter().names();
    String enumConstant = parameter.enumConstant();
    switch (parameter.skew()) {
      case REQUIRED:
        String name = enumConstant + " (" + String.join(", ", dashedNames) + ")";
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
    String enumConstant = parameter.enumConstant();
    switch (parameter.skew()) {
      case REQUIRED:
        return singletonList(CodeBlock.of(".orElseThrow(() -> $N($S))",
            missingRequiredMethod.get(), enumConstant));
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
