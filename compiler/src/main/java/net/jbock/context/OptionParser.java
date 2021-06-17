package net.jbock.context;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import net.jbock.common.Constants;
import net.jbock.common.Util;
import net.jbock.util.ExToken;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.common.Constants.STRING;
import static net.jbock.common.Constants.STRING_ITERATOR;

/**
 * Generates the inner class OptionParser and its subtypes.
 */
@ContextScope
public final class OptionParser {

  private final GeneratedTypes generatedTypes;
  private final NamedOptions namedOptions;
  private final ReadOptionArgumentMethod readOptionArgumentMethod;
  private final CommonFields commonFields;
  private final Util util;
  private final FlagParser flagParser;

  @Inject
  OptionParser(
      GeneratedTypes generatedTypes,
      NamedOptions namedOptions,
      ReadOptionArgumentMethod readOptionArgumentMethod,
      CommonFields commonFields,
      Util util,
      FlagParser flagParser) {
    this.generatedTypes = generatedTypes;
    this.namedOptions = namedOptions;
    this.readOptionArgumentMethod = readOptionArgumentMethod;
    this.commonFields = commonFields;
    this.util = util;
    this.flagParser = flagParser;
  }

  List<TypeSpec> define() {
    List<TypeSpec> result = new ArrayList<>();
    result.add(TypeSpec.classBuilder(generatedTypes.optionParserType())
        .addMethod(readMethodAbstract())
        .addMethod(streamMethodAbstract())
        .addModifiers(PRIVATE, STATIC, ABSTRACT)
        .build());
    if (namedOptions.anyFlags()) {
      result.add(flagParser.define());
    }
    if (namedOptions.anyRepeatable()) {
      result.add(TypeSpec.classBuilder(generatedTypes.repeatableOptionParserType())
          .superclass(generatedTypes.optionParserType())
          .addField(commonFields.values())
          .addMethod(readMethodRepeatable(commonFields.values()))
          .addMethod(streamMethodRepeatable(commonFields.values()))
          .addModifiers(PRIVATE, STATIC).build());
    }
    if (namedOptions.anyRegular()) {
      result.add(TypeSpec.classBuilder(generatedTypes.regularOptionParserType())
          .superclass(generatedTypes.optionParserType())
          .addField(commonFields.value())
          .addMethod(readMethodRegular(commonFields.value()))
          .addMethod(streamMethodRegular(commonFields.value()))
          .addModifiers(PRIVATE, STATIC).build());
    }
    return result;
  }

  private MethodSpec readMethodAbstract() {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    return MethodSpec.methodBuilder("read")
        .addException(ExToken.class)
        .addParameters(asList(token, it))
        .addModifiers(ABSTRACT)
        .returns(namedOptions.readMethodReturnType())
        .build();
  }

  private MethodSpec readMethodRepeatable(FieldSpec values) {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    CodeBlock.Builder code = CodeBlock.builder();
    code.addStatement("if ($N == null) $N = new $T<>()", values, values, ArrayList.class);
    code.addStatement("values.add($N($N, $N))", readOptionArgumentMethod.get(), token, it);
    if (namedOptions.unixClusteringSupported()) {
      code.addStatement("return null");
    }
    return MethodSpec.methodBuilder("read")
        .addException(ExToken.class)
        .addParameters(asList(token, it))
        .addCode(code.build())
        .returns(namedOptions.readMethodReturnType())
        .build();
  }

  private MethodSpec readMethodRegular(FieldSpec value) {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec it = ParameterSpec.builder(Constants.STRING_ITERATOR, "it").build();
    CodeBlock.Builder code = CodeBlock.builder();
    code.add("if ($N != null)\n", value).indent()
        .addStatement(util.throwRepetitionErrorStatement(token))
        .unindent();
    code.addStatement("$N = $N($N, $N)", value, readOptionArgumentMethod.get(), token, it);
    if (namedOptions.unixClusteringSupported()) {
      code.addStatement("return null");
    }
    return MethodSpec.methodBuilder("read")
        .addException(ExToken.class)
        .addCode(code.build())
        .returns(namedOptions.readMethodReturnType())
        .addParameters(asList(token, it)).build();
  }


  MethodSpec streamMethodAbstract() {
    ParameterizedTypeName streamOfString = ParameterizedTypeName.get(Stream.class, String.class);
    return MethodSpec.methodBuilder("stream")
        .returns(streamOfString)
        .addModifiers(ABSTRACT)
        .build();
  }

  MethodSpec streamMethodRegular(FieldSpec value) {
    ParameterizedTypeName streamOfString = ParameterizedTypeName.get(Stream.class, String.class);
    return MethodSpec.methodBuilder("stream")
        .returns(streamOfString)
        .addStatement("return $N == null ? $T.empty() : $T.of($N)", value, Stream.class, Stream.class, value)
        .build();
  }

  MethodSpec streamMethodRepeatable(FieldSpec values) {
    ParameterizedTypeName streamOfString = ParameterizedTypeName.get(Stream.class, String.class);
    return MethodSpec.methodBuilder("stream")
        .returns(streamOfString)
        .addStatement("return $N == null ? $T.empty() : $N.stream()", values, Stream.class, values)
        .build();
  }
}
