package net.jbock.compiler.view;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import net.jbock.compiler.Context;

import java.util.ArrayList;
import java.util.List;

import static com.squareup.javapoet.MethodSpec.constructorBuilder;
import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.ParameterSpec.builder;
import static com.squareup.javapoet.TypeSpec.classBuilder;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * Defines the inner class ParseResult.
 */
final class ParseResult {

  private final Context context;

  private final FieldSpec result;

  private ParseResult(Context context, FieldSpec result) {
    this.context = context;
    this.result = result;
  }

  static ParseResult create(Context context) {
    FieldSpec result = FieldSpec.builder(context.sourceType(),
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
    context.helpRequestedType()
        .map(this::defineHelpRequestedResult)
        .ifPresent(result::add);
    return result;
  }

  private TypeSpec defineHelpRequestedResult(ClassName helpRequestedType) {
    return classBuilder(helpRequestedType)
        .superclass(context.parseResultType())
        .addModifiers(STATIC, FINAL)
        .addModifiers(context.getAccessModifiers())
        .build();
  }

  private TypeSpec defineErrorResult() {
    ParameterSpec paramError = builder(RuntimeException.class, "error").build();
    FieldSpec fieldError = FieldSpec.builder(paramError.type, paramError.name, PRIVATE, FINAL).build();
    return classBuilder(context.parsingFailedType())
        .superclass(context.parseResultType())
        .addField(fieldError)
        .addMethod(constructorBuilder()
            .addModifiers(PRIVATE)
            .addParameter(paramError)
            .addStatement("this.$N = $N", fieldError, paramError)
            .build())
        .addModifiers(STATIC, FINAL)
        .addModifiers(context.getAccessModifiers())
        .addMethod(methodBuilder("getError")
            .addStatement("return $N", fieldError)
            .addModifiers(context.getAccessModifiers())
            .returns(fieldError.type)
            .build())
        .build();
  }

  private TypeSpec defineSuccessResult() {
    ParameterSpec paramResult = builder(result.type, result.name).build();
    return classBuilder(context.parsingSuccessType())
        .superclass(context.parseResultType())
        .addField(result)
        .addMethod(constructorBuilder()
            .addModifiers(PRIVATE)
            .addParameter(paramResult)
            .addStatement("this.$N = $N", result, paramResult)
            .build())
        .addModifiers(STATIC, FINAL)
        .addModifiers(context.getAccessModifiers())
        .addMethod(getResultMethod())
        .build();
  }

  private MethodSpec getResultMethod() {
    return methodBuilder("getResult")
        .addStatement("return $N", result)
        .returns(context.sourceType())
        .addModifiers(context.getAccessModifiers())
        .build();
  }
}
