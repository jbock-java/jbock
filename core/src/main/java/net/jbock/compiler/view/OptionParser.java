  package net.jbock.compiler.view;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import net.jbock.compiler.Constants;
import net.jbock.compiler.Context;
import net.jbock.compiler.parameter.NamedOption;
import net.jbock.compiler.parameter.Parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.squareup.javapoet.ParameterSpec.builder;
import static com.squareup.javapoet.TypeName.BOOLEAN;
import static java.util.Arrays.asList;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Constants.LIST_OF_STRING;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Constants.STRING_ITERATOR;
import static net.jbock.compiler.view.ParserState.throwRepetitionErrorStatement;

/**
 * Generates the inner class OptionParser and its subtypes.
 */
final class OptionParser {

  static List<TypeSpec> define(Context context) {
    FieldSpec values = FieldSpec.builder(LIST_OF_STRING, "values")
        .build();
    FieldSpec value = FieldSpec.builder(STRING, "value")
        .build();
    FieldSpec seen = FieldSpec.builder(BOOLEAN, "seen")
        .build();
    List<TypeSpec> result = new ArrayList<>();
    result.add(TypeSpec.classBuilder(context.optionParserType())
        .addMethod(readMethodAbstract(context))
        .addMethod(streamMethodAbstract())
        .addModifiers(PRIVATE, STATIC, ABSTRACT)
        .build());
    boolean anyRepeatable = context.options().stream().anyMatch(NamedOption::isRepeatable);
    boolean anyRegular = context.options().stream().anyMatch(option -> option.isOptional() || option.isRequired());
    boolean anyFlags = context.options().stream().anyMatch(NamedOption::isFlag);
    if (anyFlags) {
      result.add(TypeSpec.classBuilder(context.flagParserType())
          .superclass(context.optionParserType())
          .addField(seen)
          .addMethod(readMethodFlag(context, seen))
          .addMethod(streamMethodFlag(seen))
          .addModifiers(PRIVATE, STATIC).build());
    }
    if (anyRepeatable) {
      result.add(TypeSpec.classBuilder(context.repeatableOptionParserType())
          .superclass(context.optionParserType())
          .addField(values)
          .addMethod(readMethodRepeatable(context, values))
          .addMethod(streamMethodRepeatable(values))
          .addModifiers(PRIVATE, STATIC).build());
    }
    if (anyRegular) {
      result.add(TypeSpec.classBuilder(context.regularOptionParserType())
          .superclass(context.optionParserType())
          .addField(value)
          .addMethod(readMethodRegular(context, value))
          .addMethod(streamMethodRegular(value))
          .addModifiers(PRIVATE, STATIC).build());
    }
    return result;
  }

  private static MethodSpec readMethodAbstract(Context context) {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    ParameterSpec option = builder(context.optionType(), "option").build();
    return MethodSpec.methodBuilder("read")
        .addParameters(asList(option, token, it))
        .addModifiers(ABSTRACT)
        .build();
  }

  private static MethodSpec readMethodRepeatable(Context context, FieldSpec values) {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    ParameterSpec option = builder(context.optionType(), "option").build();
    return MethodSpec.methodBuilder("read")
        .addParameters(asList(option, token, it))
        .addStatement("if ($N == null) $N = new $T<>()", values, values, ArrayList.class)
        .addStatement("values.add(readOptionArgument($N, $N))", token, it)
        .build();
  }

  private static MethodSpec readMethodRegular(Context context, FieldSpec value) {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec it = ParameterSpec.builder(Constants.STRING_ITERATOR, "it").build();
    ParameterSpec option = builder(context.optionType(), "option").build();
    CodeBlock.Builder code = CodeBlock.builder();
    code.add("if ($N != null)\n", value).indent()
        .addStatement(throwRepetitionErrorStatement(option))
        .unindent();

    code.addStatement("$N = readOptionArgument($N, $N)", value, token, it);

    return MethodSpec.methodBuilder("read")
        .addCode(code.build())
        .addParameters(asList(option, token, it)).build();
  }


  private static MethodSpec readMethodFlag(Context context, FieldSpec seen) {
    ParameterSpec token = ParameterSpec.builder(Constants.STRING, "token").build();
    ParameterSpec it = ParameterSpec.builder(Constants.STRING_ITERATOR, "it").build();
    ParameterSpec option = builder(context.optionType(), "option").build();
    CodeBlock.Builder code = CodeBlock.builder();

    code.add("if ($N.charAt(1) != '-' && $N.length() > 2 || $N.contains($S))\n", token, token, token, "=").indent()
        .addStatement("throw new $T($S + $N)", RuntimeException.class, "Invalid token: ", token)
        .unindent();
    code.add("if ($N)\n", seen).indent()
        .addStatement(throwRepetitionErrorStatement(option))
        .unindent();
    code.addStatement("$N = $L", seen, true);
    return MethodSpec.methodBuilder("read")
        .addCode(code.build())
        .addParameters(asList(option, token, it)).build();
  }

  static MethodSpec streamMethodAbstract() {
    ParameterizedTypeName streamOfString = ParameterizedTypeName.get(Stream.class, String.class);
    return MethodSpec.methodBuilder("stream")
        .returns(streamOfString)
        .addModifiers(ABSTRACT)
        .build();
  }

  static MethodSpec streamMethodRegular(FieldSpec value) {
    ParameterizedTypeName streamOfString = ParameterizedTypeName.get(Stream.class, String.class);
    return MethodSpec.methodBuilder("stream")
        .returns(streamOfString)
        .addStatement("return $N == null ? $T.empty() : $T.of($N)", value, Stream.class, Stream.class, value)
        .build();
  }

  static MethodSpec streamMethodRepeatable(FieldSpec values) {
    ParameterizedTypeName streamOfString = ParameterizedTypeName.get(Stream.class, String.class);
    return MethodSpec.methodBuilder("stream")
        .returns(streamOfString)
        .addStatement("return $N == null ? $T.empty() : $N.stream()", values, Stream.class, values)
        .build();
  }

  static MethodSpec streamMethodFlag(FieldSpec seen) {
    ParameterizedTypeName streamOfString = ParameterizedTypeName.get(Stream.class, String.class);
    return MethodSpec.methodBuilder("stream")
        .returns(streamOfString)
        .addStatement("return $N ? $T.of($S) : $T.empty()", seen, Stream.class, "", Stream.class)
        .build();
  }
}
