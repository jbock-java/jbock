package net.jbock.compiler;

import net.jbock.com.squareup.javapoet.ArrayTypeName;
import net.jbock.com.squareup.javapoet.FieldSpec;
import net.jbock.com.squareup.javapoet.MethodSpec;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.TypeSpec;

import java.util.ResourceBundle;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.com.squareup.javapoet.MethodSpec.methodBuilder;
import static net.jbock.com.squareup.javapoet.TypeName.INT;
import static net.jbock.com.squareup.javapoet.TypeName.OBJECT;
import static net.jbock.com.squareup.javapoet.TypeSpec.classBuilder;

/**
 * Defines the inner class IndentPrinter.
 */
final class Messages {

  private final FieldSpec resourceBundle = FieldSpec.builder(ResourceBundle.class, "resourceBundle")
      .addModifiers(FINAL).build();

  private final Context context;

  private final FieldSpec br = FieldSpec.builder(Pattern.class, "br")
      .initializer("$T.compile($S)", Pattern.class, "\\\\r?\\\\n")
      .addModifiers(FINAL)
      .build();

  private Messages(Context context) {
    this.context = context;
  }

  static Messages create(Context context) {
    return new Messages(context);
  }

  TypeSpec define() {
    return classBuilder(context.messagesType())
        .addFields(asList(br, resourceBundle))
        .addMethod(privateConstructor())
        .addMethod(getMessageMethod())
        .addModifiers(PRIVATE, STATIC).build();
  }

  private MethodSpec getMessageMethod() {
    ParameterSpec defaultValue = ParameterSpec.builder(ArrayTypeName.of(String.class), "defaultValue").build();
    ParameterSpec key = ParameterSpec.builder(String.class, "key").build();
    return methodBuilder("getMessage")
        .addParameter(key)
        .addParameter(defaultValue)
        .returns(ArrayTypeName.of(String.class))
        .beginControlFlow("if ($N == null || !$N.containsKey($N))", resourceBundle, resourceBundle, key)
        .addStatement("return $N", defaultValue)
        .endControlFlow()
        .addStatement("return $N.split($N.getString($N), -1)", br, resourceBundle, key)
        .build();
  }

  private MethodSpec privateConstructor() {
    ParameterSpec param = ParameterSpec.builder(resourceBundle.type, resourceBundle.name).build();
    return MethodSpec.constructorBuilder()
        .addParameter(param)
        .addStatement("this.$N = $N", resourceBundle, param)
        .build();
  }
}
