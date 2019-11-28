package net.jbock.compiler.view;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import net.jbock.compiler.Context;

import java.util.Arrays;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.TypeSpec.classBuilder;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Constants.LIST_OF_STRING;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Constants.STRING_TO_STRING_MAP;

/**
 * Defines the inner class Messages.
 */
final class Messages {

  private final FieldSpec resourceBundle = FieldSpec.builder(
      STRING_TO_STRING_MAP, "messages")
      .addModifiers(FINAL).build();

  private final Context context;

  private Messages(Context context) {
    this.context = context;
  }

  static Messages create(Context context) {
    return new Messages(context);
  }

  TypeSpec define() {
    return classBuilder(context.messagesType())
        .addField(resourceBundle)
        .addMethod(privateConstructor())
        .addMethod(getMessageMethod())
        .addModifiers(PRIVATE, STATIC).build();
  }

  private MethodSpec getMessageMethod() {
    ParameterSpec key = ParameterSpec.builder(String.class, "key").build();
    ParameterSpec defaultValue = ParameterSpec.builder(LIST_OF_STRING, "defaultValue").build();
    return methodBuilder("getMessage")
        .addStatement("return $N.getOrDefault($N, $T.join($S, $N))",
            resourceBundle, key, String.class, " ", defaultValue)
        .addParameters(Arrays.asList(key, defaultValue))
        .returns(STRING)
        .build();
  }

  private MethodSpec privateConstructor() {
    ParameterSpec paramBundle = ParameterSpec.builder(resourceBundle.type, resourceBundle.name).build();
    return MethodSpec.constructorBuilder()
        .addParameter(paramBundle)
        .addStatement("this.$N = $N", resourceBundle, paramBundle)
        .build();
  }
}
