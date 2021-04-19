package net.jbock.compiler.view;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import net.jbock.compiler.Context;
import net.jbock.compiler.GeneratedTypes;

import javax.inject.Inject;
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

  private final GeneratedTypes generatedTypes;

  private final FieldSpec result;

  @Inject
  ParseResult(Context context, GeneratedTypes generatedTypes) {
    this.context = context;
    this.result = FieldSpec.builder(generatedTypes.sourceType(), "result", PRIVATE, FINAL).build();
    this.generatedTypes = generatedTypes;
  }

  List<TypeSpec> defineResultTypes() {
    TypeSpec.Builder spec = classBuilder(generatedTypes.parseResultType())
        .addMethod(constructorBuilder().addModifiers(PRIVATE).build())
        .addModifiers(ABSTRACT, STATIC)
        .addModifiers(context.getAccessModifiers());
    List<TypeSpec> result = new ArrayList<>();
    result.add(spec.build());
    result.add(defineErrorResult());
    result.add(defineSuccessResult());
    generatedTypes.helpRequestedType()
        .map(this::defineHelpRequestedResult)
        .ifPresent(result::add);
    return result;
  }

  private TypeSpec defineHelpRequestedResult(ClassName helpRequestedType) {
    return classBuilder(helpRequestedType)
        .superclass(generatedTypes.parseResultType())
        .addModifiers(STATIC, FINAL)
        .addModifiers(context.getAccessModifiers())
        .build();
  }

  private TypeSpec defineErrorResult() {
    ParameterSpec paramError = builder(RuntimeException.class, "error").build();
    FieldSpec fieldError = FieldSpec.builder(paramError.type, paramError.name, PRIVATE, FINAL).build();
    return classBuilder(generatedTypes.parsingFailedType())
        .superclass(generatedTypes.parseResultType())
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
    return classBuilder(generatedTypes.parsingSuccessType())
        .superclass(generatedTypes.parseResultType())
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
        .returns(generatedTypes.sourceType())
        .addModifiers(context.getAccessModifiers())
        .build();
  }
}
