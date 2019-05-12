package net.jbock.compiler;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.TypeSpec.classBuilder;
import static java.util.Arrays.asList;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * Defines the inner class Messages.
 */
final class Messages {

  private final FieldSpec resourceBundle = FieldSpec.builder(ResourceBundle.class, "resourceBundle")
      .addModifiers(FINAL).build();

  private final Context context;

  private final FieldSpec br = FieldSpec.builder(Pattern.class, "br")
      .initializer("$T.compile($S)", Pattern.class, "\\r?\\n")
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
        .addMethod(getMessageMethodList())
        .addModifiers(PRIVATE, STATIC).build();
  }

  private MethodSpec getMessageMethodList() {
    ParameterSpec defaultValue = ParameterSpec.builder(Constants.LIST_OF_STRING, "defaultValue").build();
    ParameterSpec key = ParameterSpec.builder(String.class, "key").build();
    MethodSpec.Builder spec = methodBuilder("getMessage");
    spec.addCode(sanityChecks(defaultValue, key));
    spec.addStatement("return $T.asList($N.split($N.getString($N), -1))", Arrays.class, br, resourceBundle, key);
    return spec.addParameter(key)
        .addParameter(defaultValue)
        .returns(Constants.LIST_OF_STRING)
        .build();
  }

  private MethodSpec getMessageMethod() {
    ParameterSpec defaultValue = ParameterSpec.builder(Constants.STRING, "defaultValue").build();
    ParameterSpec key = ParameterSpec.builder(String.class, "key").build();
    MethodSpec.Builder spec = methodBuilder("getMessage");
    spec.addCode(sanityChecks(defaultValue, key));
    spec.addStatement("return $N.getString($N)", resourceBundle, key);
    return spec.addParameter(key)
        .addParameter(defaultValue)
        .returns(Constants.STRING)
        .build();
  }


  private CodeBlock sanityChecks(ParameterSpec defaultValue, ParameterSpec key) {
    CodeBlock.Builder code = CodeBlock.builder();
    code.beginControlFlow("if ($N == null)", resourceBundle)
        .addStatement("return $N", defaultValue)
        .endControlFlow();
    code.beginControlFlow("if ($N == null || $N.isEmpty())", key, key)
        .addStatement("return $N", defaultValue)
        .endControlFlow();
    code.beginControlFlow("if (!$N.containsKey($N))", resourceBundle, key)
        .addStatement("return $N", defaultValue)
        .endControlFlow();
    return code.build();
  }

  private MethodSpec privateConstructor() {
    ParameterSpec paramBundle = ParameterSpec.builder(resourceBundle.type, resourceBundle.name).build();
    return MethodSpec.constructorBuilder()
        .addParameter(paramBundle)
        .addStatement("this.$N = $N", resourceBundle, paramBundle)
        .build();
  }
}
