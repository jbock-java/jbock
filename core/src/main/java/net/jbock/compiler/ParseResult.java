package net.jbock.compiler;

import net.jbock.com.squareup.javapoet.FieldSpec;
import net.jbock.com.squareup.javapoet.MethodSpec;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.TypeName;
import net.jbock.com.squareup.javapoet.TypeSpec;

import java.util.Optional;

import static java.util.Arrays.asList;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.com.squareup.javapoet.TypeSpec.classBuilder;
import static net.jbock.compiler.Parser.addPublicIfNecessary;
import static net.jbock.compiler.Util.optionalOf;

/**
 * Defines the inner class ParseResult.
 */
final class ParseResult {

  private final Context context;

  final FieldSpec result;
  final FieldSpec success;

  private ParseResult(Context context, FieldSpec result, FieldSpec success) {
    this.context = context;
    this.result = result;
    this.success = success;
  }

  static ParseResult create(Context context) {
    FieldSpec result = FieldSpec.builder(TypeName.get(context.sourceType.asType()), "result",
        PRIVATE, FINAL).build();
    FieldSpec success = FieldSpec.builder(TypeName.BOOLEAN, "success",
        PRIVATE, FINAL).build();
    return new ParseResult(context, result, success);
  }

  TypeSpec define() {
    TypeSpec.Builder spec = classBuilder(context.parseResultType())
        .addFields(asList(result, success))
        .addMethod(addPublicIfNecessary(context, resultMethod()))
        .addMethod(addPublicIfNecessary(context, errorMethod()))
        .addMethod(addPublicIfNecessary(context, helpPrintedMethod()))
        .addMethod(privateConstructor())
        .addModifiers(STATIC);
    if (context.sourceType.getModifiers().contains(PUBLIC)) {
      spec.addModifiers(PUBLIC);
    }
    return spec.build();
  }

  private MethodSpec.Builder errorMethod() {
    return MethodSpec.methodBuilder("error")
        .addStatement("return !$N", success)
        .returns(TypeName.BOOLEAN);
  }

  private MethodSpec.Builder helpPrintedMethod() {
    return MethodSpec.methodBuilder("helpPrinted")
        .addStatement("return $N == null && $N", result, success)
        .returns(TypeName.BOOLEAN);
  }

  private MethodSpec.Builder resultMethod() {
    return MethodSpec.methodBuilder("result")
        .addStatement("return $T.ofNullable($N)", Optional.class, result)
        .returns(optionalOf(TypeName.get(context.sourceType.asType())));
  }

  private MethodSpec privateConstructor() {
    ParameterSpec paramResult = ParameterSpec.builder(result.type, result.name).build();
    ParameterSpec paramSuccess = ParameterSpec.builder(success.type, success.name).build();
    return MethodSpec.constructorBuilder()
        .addParameter(paramResult)
        .addParameter(paramSuccess)
        .addStatement("this.$N = $N", result, paramResult)
        .addStatement("this.$N = $N", success, paramSuccess)
        .addModifiers(PRIVATE)
        .build();
  }
}
