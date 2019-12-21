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
    FieldSpec result = FieldSpec.builder(context.sourceElement(),
        "result", PRIVATE, FINAL).build();
    return new ParseResult(context, result);
  }

  List<TypeSpec> defineResultTypes() {
    TypeSpec.Builder spec = classBuilder(context.parseResultType())
        .addMethod(constructorBuilder().addModifiers(PRIVATE).build())
        .addModifiers(ABSTRACT)
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
        .addModifiers(FINAL)
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
            .addParameter(paramError)
            .addStatement("this.$N = $N", fieldError, paramError)
            .build())
        .addModifiers(FINAL)
        .addModifiers(context.getAccessModifiers())
        .addMethod(methodBuilder("getError")
            .addStatement("return $N", fieldError)
            .addModifiers(context.getAccessModifiers())
            .returns(fieldError.type)
            .build())
        .build();
  }

  private TypeSpec defineSuccessResult() {
    return classBuilder(context.parsingSuccessType())
        .superclass(context.parseResultType())
        .addField(result)
        .addMethod(successConstructor())
        .addModifiers(FINAL)
        .addModifiers(context.getAccessModifiers())
        .addMethod(getResultMethod())
        .build();
  }

  private MethodSpec getResultMethod() {
    return methodBuilder("getResult")
        .addStatement("return $N", result)
        .returns(context.sourceElement())
        .addModifiers(context.getAccessModifiers())
        .build();
  }

  private MethodSpec successConstructor() {
    ParameterSpec paramResult = builder(result.type, result.name).build();
    return constructorBuilder()
        .addParameter(paramResult)
        .addStatement("this.$N = $N", result, paramResult)
        .build();
  }
}
