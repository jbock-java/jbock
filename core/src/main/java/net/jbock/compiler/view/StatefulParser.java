package net.jbock.compiler.view;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import net.jbock.coerce.ConvertedParameter;
import net.jbock.compiler.Context;
import net.jbock.compiler.GeneratedTypes;
import net.jbock.compiler.parameter.AbstractParameter;

import javax.inject.Inject;

import java.util.ArrayList;
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

    CodeBlock.Builder args = CodeBlock.builder().add("\n");
    for (int j = 0; j < context.regularParameters().size(); j++) {
      if (j > 0) {
        args.add(",\n");
      }
      ConvertedParameter<? extends AbstractParameter> param = context.regularParameters().get(j);
      args.add(extractExpression(param));
    }
    context.repeatableParam().ifPresent(param -> {
      if (!context.regularParameters().isEmpty()) {
        args.add(",\n");
      }
      args.add(CodeBlock.builder()
          .add("$N.stream()\n", rest).indent()
          .add(".map($L)\n", param.mapExpr())
          .add(".collect($T.toList())", Collectors.class).unindent()
          .build());
    });
    return MethodSpec.methodBuilder("build")
        .addStatement("return new $T($L)", generatedTypes.implType(), args.build())
        .returns(generatedTypes.sourceType())
        .build();
  }

  private CodeBlock extractExpression(ConvertedParameter<? extends AbstractParameter> param) {
    return getStreamExpression(param)
        .add("\n").indent()
        .add(".map($L)", param.mapExpr())
        .add(param.tailExpr()).unindent()
        .build();
  }

  private CodeBlock.Builder getStreamExpression(ConvertedParameter<? extends AbstractParameter> param) {
    if (param.parameter().isPositional()) {
      return CodeBlock.builder().add(
          "$T.ofNullable($N[$L])", Optional.class, paramParsersField,
          param.parameter().positionalIndex().orElseThrow(AssertionError::new));
    }
    return CodeBlock.builder().add(
        "$N.get($T.$N).stream()", optionParsersField,
        generatedTypes.optionType(), param.enumConstant());
  }
}
