package net.jbock.compiler;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import static java.util.Arrays.asList;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static com.squareup.javapoet.TypeName.BOOLEAN;
import static net.jbock.compiler.Constants.LIST_OF_STRING;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Util.optionalOf;

/**
 * Generates the OptionParser class.
 */
final class OptionParser {

  static TypeSpec define(Context context) {
    CodeBlock defaultImpl = CodeBlock.builder()
        .addStatement("throw new $T($S)", UnsupportedOperationException.class, "Oh no!")
        .build();
    FieldSpec option = FieldSpec.builder(context.optionType(), "option", FINAL).build();
    return TypeSpec.classBuilder(context.optionParserType())
        .addMethod(readMethod())
        .addMethod(MethodSpec.methodBuilder("value")
            .returns(optionalOf(STRING))
            .addCode(defaultImpl)
            .build())
        .addMethod(MethodSpec.methodBuilder("values")
            .returns(LIST_OF_STRING)
            .addCode(defaultImpl)
            .build())
        .addMethod(MethodSpec.methodBuilder("flag")
            .returns(BOOLEAN)
            .addCode(defaultImpl)
            .build())
        .addField(option)
        .addMethod(constructor(option))
        .addModifiers(PRIVATE, ABSTRACT, STATIC)
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
