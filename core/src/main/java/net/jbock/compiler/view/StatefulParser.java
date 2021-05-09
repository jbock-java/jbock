package net.jbock.compiler.view;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
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

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.squareup.javapoet.TypeName.BOOLEAN;
import static com.squareup.javapoet.TypeName.INT;
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

  private final FieldSpec optionParsersField;

  private final FieldSpec paramParsersField;

  private final FieldSpec endOfOptionParsing = FieldSpec.builder(BOOLEAN, "endOfOptionParsing").build();

  private final FieldSpec rest = FieldSpec.builder(LIST_OF_STRING, "rest")
      .initializer("new $T<>()", ArrayList.class)
      .build();

  @Inject
  StatefulParser(Context context, GeneratedTypes generatedTypes, ParseMethod parseMethod) {
    this.context = context;
    this.generatedTypes = generatedTypes;
    this.parseMethod = parseMethod;
    ClassName optionType = generatedTypes.optionType();

    // stateful parsers
    this.optionParsersField = FieldSpec.builder(mapOf(optionType, generatedTypes.optionParserType()), "optionParsers")
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
    if (!context.options().isEmpty()) {
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
    ParameterSpec option = ParameterSpec.builder(generatedTypes.optionType(), "option").build();
    CodeBlock.Builder code = CodeBlock.builder();
    if (!context.isSuperCommand()) {
      code.add("if ($N)\n", endOfOptionParsing).indent()
          .addStatement("return false")
          .unindent();
    }
    code.addStatement("$T $N = tryReadOption($N)", generatedTypes.optionType(), option, token);
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
    ParameterSpec option = ParameterSpec.builder(generatedTypes.optionType(), "option").build();
    CodeBlock.Builder code = CodeBlock.builder();
    if (!context.isSuperCommand()) {
      code.add("if ($N)\n", endOfOptionParsing).indent()
          .addStatement("return false")
          .unindent();
    }
    code.addStatement("$T $N = tryReadOption($N)", generatedTypes.optionType(), option, token);
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
    FieldSpec optionsByName = FieldSpec.builder(mapOf(STRING, generatedTypes.optionType()),
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
        .returns(generatedTypes.optionType()).build();
  }

  private MethodSpec buildMethod() {

    List<CodeBlock> code = new ArrayList<>();
    for (ConvertedParameter<NamedOption> option : context.options()) {
      CodeBlock streamExpression = streamExpressionOption(option);
      code.add(extractExpressionOption(streamExpression, option));
    }
    for (ConvertedParameter<PositionalParameter> param : context.regularParams()) {
      CodeBlock streamExpression = streamExpressionParameter(param);
      code.add(extractExpressionParameter(streamExpression, param));
    }
    context.repeatableParam()
        .map(param -> CodeBlock.builder()
            .add("$N.stream()\n", rest).indent()
            .add(".map($L)\n", param.mapExpr())
            .add(".collect($T.toList())", Collectors.class).unindent()
            .build())
        .ifPresent(code::add);
    return MethodSpec.methodBuilder("build")
        .addStatement("return new $T($L)", generatedTypes.implType(), joinCodeBlocks(code))
        .returns(generatedTypes.sourceType())
        .build();
  }

  private CodeBlock extractExpressionOption(
      CodeBlock streamExpression,
      ConvertedParameter<NamedOption> option) {
    return CodeBlock.builder()
        .add(streamExpression).add("\n").indent()
        .add(".map($L)", option.mapExpr())
        .add(tailExpressionOption(option)).unindent()
        .build();
  }

  private CodeBlock extractExpressionParameter(
      CodeBlock streamExpression,
      ConvertedParameter<PositionalParameter> param) {
    return CodeBlock.builder()
        .add(streamExpression).add("\n").indent()
        .add(".map($L)", param.mapExpr())
        .add(tailExpressionParameter(param)).unindent()
        .build();
  }

  private CodeBlock streamExpressionOption(ConvertedParameter<NamedOption> option) {
    return CodeBlock.builder().add(
        "$N.get($T.$N).stream()", optionParsersField,
        generatedTypes.optionType(), option.enumConstant()).build();
  }

  private CodeBlock streamExpressionParameter(ConvertedParameter<PositionalParameter> parameter) {
    return CodeBlock.builder().add(
        "$T.ofNullable($N[$L])", Optional.class, paramParsersField,
        parameter.parameter().position()).build();
  }

  private CodeBlock tailExpressionOption(ConvertedParameter<NamedOption> parameter) {
    List<String> dashedNames = parameter.parameter().dashedNames();
    String enumConstant = parameter.enumConstant();
    switch (parameter.skew()) {
      case REQUIRED:
        String name = enumConstant + " (" + String.join(", ", dashedNames) + ")";
        return CodeBlock.builder()
            .add("\n.findAny()")
            .add("\n.orElseThrow(() -> missingRequired($S))", name)
            .build();
      case OPTIONAL:
        return CodeBlock.of("\n.findAny()");
      case REPEATABLE:
        return CodeBlock.of(".collect($T.toList())", Collectors.class);
      case FLAG:
        return CodeBlock.of(".findAny().isPresent()");
      default:
        throw new UnsupportedOperationException("unknown skew: " + parameter.skew());
    }
  }

  private CodeBlock tailExpressionParameter(ConvertedParameter<PositionalParameter> parameter) {
    String enumConstant = parameter.enumConstant();
    switch (parameter.skew()) {
      case REQUIRED:
        return CodeBlock.of("\n.orElseThrow(() -> missingRequired($S))", enumConstant);
      case OPTIONAL:
        return CodeBlock.builder().build();
      case REPEATABLE:
        return CodeBlock.of(".collect($T.toList())", Collectors.class);
      case FLAG:
        return CodeBlock.of(".findAny().isPresent()");
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
}
