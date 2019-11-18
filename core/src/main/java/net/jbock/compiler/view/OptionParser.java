package net.jbock.compiler.view;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import net.jbock.compiler.Constants;
import net.jbock.compiler.Context;

import static com.squareup.javapoet.TypeName.BOOLEAN;
import static java.util.Arrays.asList;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Constants.OPTIONAL_STRING;
import static net.jbock.compiler.Constants.STREAM_OF_STRING;
import static net.jbock.compiler.Constants.STRING;

/**
 * Generates the OptionParser class.
 */
final class OptionParser {

  static TypeSpec define(Context context) {
    FieldSpec option = FieldSpec.builder(context.optionType(), "option", FINAL).build();
    return TypeSpec.classBuilder(context.optionParserType())
        .addMethod(readMethod())
        .addMethod(MethodSpec.methodBuilder("value")
            .returns(OPTIONAL_STRING)
            .addCode(throwAssertionError())
            .build())
        .addMethod(MethodSpec.methodBuilder("values")
            .returns(STREAM_OF_STRING)
            .addCode(throwAssertionError())
            .build())
        .addMethod(MethodSpec.methodBuilder("flag")
            .returns(BOOLEAN)
            .addCode(throwAssertionError())
            .build())
        .addField(option)
        .addMethod(constructor(option))
        .addModifiers(PRIVATE, ABSTRACT, STATIC)
        .build();
  }

  private static CodeBlock throwAssertionError() {
    return CodeBlock.builder()
        .addStatement("throw new $T()", AssertionError.class)
        .build();
  }

  private static MethodSpec constructor(FieldSpec option) {
    ParameterSpec optionParam = ParameterSpec.builder(option.type, option.name).build();
    return MethodSpec.constructorBuilder()
        .addStatement("this.$N = $N", option, optionParam)
        .addParameter(optionParam)
        .build();
  }

  private static MethodSpec readMethod() {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec it = ParameterSpec.builder(Constants.STRING_ITERATOR, "it").build();
    return MethodSpec.methodBuilder("read")
        .addModifiers(ABSTRACT)
        .addParameters(asList(token, it))
        .build();
  }
}
