package net.jbock.compiler.view;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import net.jbock.compiler.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.squareup.javapoet.MethodSpec.constructorBuilder;
import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.ParameterSpec.builder;
import static com.squareup.javapoet.TypeSpec.classBuilder;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Constants.STRING;

/**
 * Defines the inner class ParseResult.
 */
final class ParseResult {

  private final Context context;

  private final FieldSpec result;
  private final FieldSpec message = FieldSpec.builder(STRING, "message", PRIVATE, FINAL).build();

  private ParseResult(Context context, FieldSpec result) {
    this.context = context;
    this.result = result;
  }

  static ParseResult create(Context context) {
    FieldSpec result = FieldSpec.builder(context.sourceElement(),
        "result", PRIVATE, FINAL).build();
    return new ParseResult(context, result);
  }

  List<TypeSpec> defineResultTypes() {
    TypeSpec.Builder spec = classBuilder(context.parseResultType())
        .addMethod(constructorBuilder().addModifiers(PRIVATE).build())
        .addModifiers(ABSTRACT, STATIC)
        .addModifiers(context.getAccessModifiers());
    List<TypeSpec> result = new ArrayList<>();
    result.add(spec.build());
    result.add(defineErrorResult());
    result.add(defineSuccessResult());
    definePrintHelpResult().ifPresent(result::add);
    return result;
  }

  private Optional<TypeSpec> definePrintHelpResult() {
    return context.helpPrintedType().map(helpPrintedType -> {
      TypeSpec.Builder spec = classBuilder(helpPrintedType)
          .superclass(context.parseResultType())
          .addMethod(constructorBuilder().addModifiers(PRIVATE).build())
          .addModifiers(STATIC, FINAL)
          .addModifiers(context.getAccessModifiers());
      return spec.build();
    });
  }

  private TypeSpec defineErrorResult() {
    ParameterSpec paramMessage = builder(STRING, message.name).build();
    return classBuilder(context.parsingFailedType())
        .superclass(context.parseResultType())
        .addField(message)
        .addMethod(constructorBuilder()
            .addParameter(paramMessage)
            .addStatement("this.$N = $T.requireNonNull($N)", message, Objects.class, paramMessage)
            .addModifiers(PRIVATE).build())
        .addModifiers(STATIC, FINAL)
        .addModifiers(context.getAccessModifiers())
        .addMethod(messageMethod())
        .build();
  }

  private TypeSpec defineSuccessResult() {
    return classBuilder(context.parsingSuccessType())
        .superclass(context.parseResultType())
        .addField(result)
        .addMethod(successConstructor())
        .addModifiers(STATIC, FINAL)
        .addModifiers(context.getAccessModifiers())
        .addMethod(resultMethod())
        .build();
  }

  private MethodSpec resultMethod() {
    return methodBuilder("result")
        .addStatement("return $N", result)
        .returns(context.sourceElement())
        .addModifiers(context.getAccessModifiers())
        .build();
  }

  private MethodSpec messageMethod() {
    return methodBuilder("message")
        .addStatement("return $N", message)
        .addModifiers(context.getAccessModifiers())
        .returns(STRING)
        .build();
  }

  private MethodSpec successConstructor() {
    ParameterSpec paramResult = builder(result.type, result.name).build();
    MethodSpec.Builder spec = constructorBuilder()
        .addParameter(paramResult);
    return spec
        .addStatement("this.$N = $T.requireNonNull($N)", result, Objects.class, paramResult)
        .addModifiers(PRIVATE)
        .build();
  }
}
