package net.jbock.compiler.view;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import net.jbock.compiler.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.squareup.javapoet.MethodSpec.constructorBuilder;
import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.ParameterSpec.builder;
import static com.squareup.javapoet.TypeSpec.classBuilder;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Constants.ENTRY_STRING_STRING;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Constants.listOf;

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
    ParameterSpec paramSynopsis = builder(STRING, "synopsis").build();
    ParameterSpec paramRows = builder(listOf(ENTRY_STRING_STRING), "rows").build();
    FieldSpec fieldSynopsis = FieldSpec.builder(paramSynopsis.type, paramSynopsis.name, PRIVATE, FINAL).build();
    FieldSpec fieldRows = FieldSpec.builder(paramRows.type, paramRows.name, PRIVATE, FINAL).build();
    return classBuilder(helpRequestedType)
        .addFields(Arrays.asList(fieldSynopsis, fieldRows))
        .superclass(context.parseResultType())
        .addMethod(constructorBuilder()
            .addParameters(Arrays.asList(paramSynopsis, paramRows))
            .addStatement("this.$N = $N", fieldSynopsis, paramSynopsis)
            .addStatement("this.$N = $N", fieldRows, paramRows)
            .addModifiers(PRIVATE).build())
        .addMethod(methodBuilder("getSynopsis")
            .addStatement("return $N", fieldSynopsis)
            .returns(fieldSynopsis.type)
            .addModifiers(context.getAccessModifiers())
            .build())
        .addMethod(methodBuilder("getRows")
            .addStatement("return $N", fieldRows)
            .returns(fieldRows.type)
            .addModifiers(context.getAccessModifiers())
            .build())
        .addModifiers(STATIC, FINAL)
        .addModifiers(context.getAccessModifiers())
        .build();
  }

  private TypeSpec defineErrorResult() {
    ParameterSpec paramSynopsis = builder(STRING, "synopsis").build();
    ParameterSpec paramRows = builder(listOf(ENTRY_STRING_STRING), "rows").build();
    ParameterSpec paramError = builder(RuntimeException.class, "error").build();
    FieldSpec fieldSynopsis = FieldSpec.builder(paramSynopsis.type, paramSynopsis.name, PRIVATE, FINAL).build();
    FieldSpec fieldRows = FieldSpec.builder(paramRows.type, paramRows.name, PRIVATE, FINAL).build();
    FieldSpec fieldError = FieldSpec.builder(paramError.type, paramError.name, PRIVATE, FINAL).build();
    return classBuilder(context.parsingFailedType())
        .superclass(context.parseResultType())
        .addFields(Arrays.asList(fieldSynopsis, fieldRows, fieldError))
        .addMethod(constructorBuilder()
            .addParameters(Arrays.asList(paramSynopsis, paramRows, paramError))
            .addStatement("this.$N = $N", fieldError, paramError)
            .addStatement("this.$N = $N", fieldSynopsis, paramSynopsis)
            .addStatement("this.$N = $N", fieldRows, paramRows)
            .addModifiers(PRIVATE).build())
        .addModifiers(STATIC, FINAL)
        .addModifiers(context.getAccessModifiers())
        .addMethod(methodBuilder("getSynopsis")
            .addStatement("return $N", fieldSynopsis)
            .returns(fieldSynopsis.type)
            .addModifiers(context.getAccessModifiers())
            .build())
        .addMethod(methodBuilder("getRows")
            .addStatement("return $N", fieldRows)
            .returns(fieldRows.type)
            .addModifiers(context.getAccessModifiers())
            .build())
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
        .addModifiers(STATIC, FINAL)
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
    MethodSpec.Builder spec = constructorBuilder()
        .addParameter(paramResult);
    return spec
        .addStatement("this.$N = $N", result, paramResult)
        .addModifiers(PRIVATE)
        .build();
  }
}
