package net.jbock.compiler.view;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import net.jbock.compiler.Context;
import net.jbock.compiler.GeneratedTypes;
import net.jbock.compiler.parameter.NamedOption;
import net.jbock.compiler.parameter.PositionalParameter;
import net.jbock.convert.ConvertedParameter;
import net.jbock.qualifier.GeneratedType;
import net.jbock.qualifier.SourceElement;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.squareup.javapoet.TypeName.BOOLEAN;
import static com.squareup.javapoet.TypeName.INT;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Constants.LIST_OF_STRING;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Constants.STRING_ITERATOR;
import static net.jbock.compiler.Constants.mapOf;
import static net.jbock.compiler.view.GeneratedClass.OPTIONS_BY_NAME;

/**
 * Defines the inner class StatefulParser
 */
final class StatefulParser {

  private final Context context;
  private final ParseMethod parseMethod;
  private final GeneratedTypes generatedTypes;
  private final GeneratedType generatedType;
  private final FieldSpec optionParsersField;
  private final FieldSpec paramParsersField;
  private final SourceElement sourceElement;
  private final List<ConvertedParameter<NamedOption>> options;

  private final FieldSpec endOfOptionParsing = FieldSpec.builder(BOOLEAN, "endOfOptionParsing").build();

  private final FieldSpec rest = FieldSpec.builder(LIST_OF_STRING, "rest")
      .initializer("new $T<>()", ArrayList.class)
      .build();

  @Inject
  StatefulParser(
      Context context,
      GeneratedTypes generatedTypes,
      GeneratedType generatedType,
      ParseMethod parseMethod,
      SourceElement sourceElement,
      List<ConvertedParameter<NamedOption>> options) {
    this.context = context;
    this.generatedTypes = generatedTypes;
    this.generatedType = generatedType;
    this.parseMethod = parseMethod;
    this.sourceElement = sourceElement;
    this.options = options;

    this.optionParsersField = FieldSpec.builder(mapOf(generatedType.optionType(), generatedTypes.optionParserType()), "optionParsers")
        .initializer("optionParsers()")
        .build();

    this.paramParsersField = FieldSpec.builder(ArrayTypeName.of(STRING), "paramParsers")
        .initializer("new $T[$L]", STRING, context.regularParams().size())
        .build();
  }

  TypeSpec define() {
    TypeSpec.Builder spec = TypeSpec.classBuilder(generatedTypes.statefulParserType())
        .addModifiers(PRIVATE, STATIC)
        .addMethod(parseMethod.parseMethod());
    if (!context.isSuperCommand()) {
      spec.addField(endOfOptionParsing);
    }
    if (!options.isEmpty()) {
      spec.addMethod(tryParseOptionMethod())
          .addMethod(tryReadOptionMethod());
      spec.addField(optionParsersField);
    }
    if (!context.regularParams().isEmpty()) {
      spec.addField(paramParsersField);
    }
    if (context.anyRepeatableParam() || context.isSuperCommand()) {
      spec.addField(rest);
    }
    spec.addMethod(buildMethod());
    return spec.build();
  }

  private MethodSpec tryParseOptionMethod() {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    return MethodSpec.methodBuilder("tryParseOption")
        .addParameter(token)
        .addParameter(it)
        .addCode(context.isUnixClusteringSupported() ?
            tryParseOptionCodeClustering(token, it) :
            tryParseOptionCodeSimple(token, it))
        .returns(BOOLEAN)
        .build();
  }

  private CodeBlock tryParseOptionCodeClustering(ParameterSpec token, ParameterSpec it) {
    ParameterSpec clusterToken = ParameterSpec.builder(STRING, "clusterToken").build();
    ParameterSpec option = ParameterSpec.builder(generatedType.optionType(), "option").build();
    CodeBlock.Builder code = CodeBlock.builder();
    if (!context.isSuperCommand()) {
      code.add("if ($N)\n", endOfOptionParsing).indent()
          .addStatement("return false")
          .unindent();
    }
    code.addStatement("$T $N = tryReadOption($N)", generatedType.optionType(), option, token);
    code.add("if ($N == null)\n", option).indent()
        .addStatement("return false")
        .unindent();
    code.addStatement("$T $N = $N", clusterToken.type, clusterToken, token);
    code.beginControlFlow("while ($N.get($N).read($N, $N))", optionParsersField, option, clusterToken, it);
    code.addStatement("$1N = '-' + $1N.substring(2, $1N.length())", clusterToken);
    code.addStatement("$N = tryReadOption($N)", option, clusterToken);
    code.add("if ($N == null)\n", option).indent()
        .addStatement("throw new $T($S + $N)", RuntimeException.class, "Invalid token: ", token)
        .unindent();
    code.endControlFlow();
    code.addStatement("return true");
    return code.build();
  }

  private CodeBlock tryParseOptionCodeSimple(ParameterSpec token, ParameterSpec it) {
    ParameterSpec option = ParameterSpec.builder(generatedType.optionType(), "option").build();
    CodeBlock.Builder code = CodeBlock.builder();
    if (!context.isSuperCommand()) {
      code.add("if ($N)\n", endOfOptionParsing).indent()
          .addStatement("return false")
          .unindent();
    }
    code.addStatement("$T $N = tryReadOption($N)", generatedType.optionType(), option, token);
    code.add("if ($N == null)\n", option).indent()
        .addStatement("return false")
        .unindent();
    code.addStatement("$N.get($N).read($N, $N)", optionParsersField, option, token, it)
        .addStatement("return true");
    return code.build();
  }

  private MethodSpec tryReadOptionMethod() {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec index = ParameterSpec.builder(INT, "index").build();
    FieldSpec optionsByName = FieldSpec.builder(mapOf(STRING, generatedType.optionType()),
        OPTIONS_BY_NAME).build();

    CodeBlock.Builder code = CodeBlock.builder();
    code.add("if ($N.length() <= 1 || $N.charAt(0) != '-')\n", token, token).indent()
        .addStatement("return null").unindent();

    code.add("if ($N.charAt(1) != '-')\n", token).indent()
        .addStatement("return $N.get($N.substring(0, 2))", optionsByName, token).unindent();

    code.addStatement("$T $N = $N.indexOf('=')", INT, index, token);
    code.add("if ($N < 0)\n", index).indent()
        .addStatement("return $N.get($N)", optionsByName, token)
        .unindent();
    code.addStatement("return $N.get($N.substring(0, $N))", optionsByName, token, index);

    return MethodSpec.methodBuilder("tryReadOption")
        .addParameter(token)
        .addCode(code.build())
        .returns(generatedType.optionType()).build();
  }

  private MethodSpec buildMethod() {

    List<CodeBlock> code = new ArrayList<>();
    for (ConvertedParameter<NamedOption> option : options) {
      CodeBlock streamExpression = streamExpressionOption(option);
      code.add(extractExpressionOption(streamExpression, option));
    }
    for (ConvertedParameter<PositionalParameter> param : context.regularParams()) {
      CodeBlock streamExpression = streamExpressionParameter(param);
      code.add(extractExpressionParameter(streamExpression, param));
    }
    context.repeatableParam()
        .map(param -> {
          List<CodeBlock> block = new ArrayList<>();
          block.add(CodeBlock.of("$N.stream()", rest));
          block.add(param.mapExpr());
          block.add(CodeBlock.of(".collect($T.toList())", Collectors.class));
          return joinIndent(block);
        })
        .ifPresent(code::add);
    return MethodSpec.methodBuilder("build")
        .addStatement("return new $T($L)", generatedTypes.implType(), joinCodeBlocks(code))
        .returns(sourceElement.typeName())
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
        "$N.get($T.$N).stream()", optionParsersField,
        generatedType.optionType(), option.enumConstant()).build();
  }

  private CodeBlock streamExpressionParameter(ConvertedParameter<PositionalParameter> parameter) {
    return CodeBlock.builder().add(
        "$T.ofNullable($N[$L])", Optional.class, paramParsersField,
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
            CodeBlock.of(".orElseThrow(() -> missingRequired($S))", name));
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
        return singletonList(CodeBlock.of(".orElseThrow(() -> missingRequired($S))", enumConstant));
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
