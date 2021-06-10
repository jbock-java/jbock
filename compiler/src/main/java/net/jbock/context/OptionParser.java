package net.jbock.context;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import net.jbock.common.Constants;
import net.jbock.util.ExToken;
import net.jbock.util.ErrTokenType;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.squareup.javapoet.TypeName.BOOLEAN;
import static com.squareup.javapoet.TypeName.VOID;
import static java.util.Arrays.asList;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.common.Constants.LIST_OF_STRING;
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

  @Inject
  OptionParser(
      GeneratedTypes generatedTypes,
      NamedOptions namedOptions,
      ReadOptionArgumentMethod readOptionArgumentMethod) {
    this.generatedTypes = generatedTypes;
    this.namedOptions = namedOptions;
    this.readOptionArgumentMethod = readOptionArgumentMethod;
  }

  List<TypeSpec> define() {
    FieldSpec values = FieldSpec.builder(LIST_OF_STRING, "values")
        .build();
    FieldSpec value = FieldSpec.builder(STRING, "value")
        .build();
    FieldSpec seen = FieldSpec.builder(BOOLEAN, "seen")
        .build();
    List<TypeSpec> result = new ArrayList<>();
    result.add(TypeSpec.classBuilder(generatedTypes.optionParserType())
        .addMethod(readMethodAbstract())
        .addMethod(streamMethodAbstract())
        .addModifiers(PRIVATE, STATIC, ABSTRACT)
        .build());
    if (namedOptions.anyFlags()) {
      result.add(TypeSpec.classBuilder(generatedTypes.flagParserType())
          .superclass(generatedTypes.optionParserType())
          .addField(seen)
          .addMethod(readMethodFlag(seen))
          .addMethod(streamMethodFlag(seen))
          .addModifiers(PRIVATE, STATIC).build());
    }
    if (namedOptions.anyRepeatable()) {
      result.add(TypeSpec.classBuilder(generatedTypes.repeatableOptionParserType())
          .superclass(generatedTypes.optionParserType())
          .addField(values)
          .addMethod(readMethodRepeatable(values))
          .addMethod(streamMethodRepeatable(values))
          .addModifiers(PRIVATE, STATIC).build());
    }
    if (namedOptions.anyRegular()) {
      result.add(TypeSpec.classBuilder(generatedTypes.regularOptionParserType())
          .superclass(generatedTypes.optionParserType())
          .addField(value)
          .addMethod(readMethodRegular(value))
          .addMethod(streamMethodRegular(value))
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
        .returns(readMethodReturnType())
        .build();
  }

  private MethodSpec readMethodRepeatable(FieldSpec values) {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    CodeBlock.Builder code = CodeBlock.builder();
    code.addStatement("if ($N == null) $N = new $T<>()", values, values, ArrayList.class);
    code.addStatement("values.add($N($N, $N))", readOptionArgumentMethod.get(), token, it);
    if (namedOptions.unixClusteringSupported()) {
      code.addStatement("return false");
    }
    return MethodSpec.methodBuilder("read")
        .addException(ExToken.class)
        .addParameters(asList(token, it))
        .addCode(code.build())
        .returns(readMethodReturnType())
        .build();
  }

  private MethodSpec readMethodRegular(FieldSpec value) {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec it = ParameterSpec.builder(Constants.STRING_ITERATOR, "it").build();
    CodeBlock.Builder code = CodeBlock.builder();
    code.add("if ($N != null)\n", value).indent()
        .addStatement(throwRepetitionErrorStatement(token))
        .unindent();
    code.addStatement("$N = $N($N, $N)", value, readOptionArgumentMethod.get(), token, it);
    if (namedOptions.unixClusteringSupported()) {
      code.addStatement("return false");
    }
    return MethodSpec.methodBuilder("read")
        .addException(ExToken.class)
        .addCode(code.build())
        .returns(readMethodReturnType())
        .addParameters(asList(token, it)).build();
  }


  private MethodSpec readMethodFlag(FieldSpec seen) {
    ParameterSpec token = ParameterSpec.builder(Constants.STRING, "token").build();
    ParameterSpec it = ParameterSpec.builder(Constants.STRING_ITERATOR, "it").build();
    return MethodSpec.methodBuilder("read")
        .addException(ExToken.class)
        .addCode(namedOptions.unixClusteringSupported() ?
            readMethodFlagCodeClustering(seen, token) :
            readMethodFlagCodeSimple(seen, token))
        .returns(readMethodReturnType())
        .addParameters(asList(token, it)).build();
  }

  private CodeBlock readMethodFlagCodeClustering(FieldSpec seen, ParameterSpec token) {
    CodeBlock.Builder code = CodeBlock.builder();
    code.add("if ($N.contains($S))\n", token, "=").indent()
        .addStatement("throw new $T($T.$L, $N)", ExToken.class, ErrTokenType.class,
            ErrTokenType.INVALID_UNIX_GROUP, token)
        .unindent();
    code.add("if ($N)\n", seen).indent()
        .addStatement(throwRepetitionErrorStatement(token))
        .unindent();
    code.addStatement("$N = $L", seen, true);
    code.addStatement("return $1N.charAt(1) != '-' && $1N.length() > 2", token);
    return code.build();
  }

  private CodeBlock readMethodFlagCodeSimple(FieldSpec seen, ParameterSpec token) {
    CodeBlock.Builder code = CodeBlock.builder();
    code.add("if ($N.charAt(1) != '-' && $N.length() > 2 || $N.contains($S))\n", token, token, token, "=").indent()
        .addStatement("throw new $T($T.$L, $N)", ExToken.class, ErrTokenType.class,
            ErrTokenType.INVALID_UNIX_GROUP, token)
        .unindent();
    code.add("if ($N)\n", seen).indent()
        .addStatement(throwRepetitionErrorStatement(token))
        .unindent();
    code.addStatement("$N = $L", seen, true);
    return code.build();
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

  MethodSpec streamMethodFlag(FieldSpec seen) {
    ParameterizedTypeName streamOfString = ParameterizedTypeName.get(Stream.class, String.class);
    return MethodSpec.methodBuilder("stream")
        .returns(streamOfString)
        .addStatement("return $N ? $T.of($S) : $T.empty()", seen, Stream.class, "", Stream.class)
        .build();
  }

  private CodeBlock throwRepetitionErrorStatement(ParameterSpec token) {
    return CodeBlock.of("throw new $T($T.$L, $N)", ExToken.class, ErrTokenType.class,
        ErrTokenType.OPTION_REPETITION, token);
  }

  private TypeName readMethodReturnType() {
    return namedOptions.unixClusteringSupported() ? BOOLEAN : VOID;
  }
}
