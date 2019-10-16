package net.jbock.compiler;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.squareup.javapoet.MethodSpec.constructorBuilder;
import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.ParameterSpec.builder;
import static com.squareup.javapoet.TypeSpec.classBuilder;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.view.Parser.addPublicIfNecessary;

/**
 * Defines the inner class ParseResult.
 */
public final class ParseResult {

  private final Context context;

  private final FieldSpec result;
  private final FieldSpec message = FieldSpec.builder(STRING, "message", PRIVATE, FINAL).build();

  private ParseResult(Context context, FieldSpec result) {
    this.context = context;
    this.result = result;
  }

  public static ParseResult create(Context context) {
    FieldSpec result = FieldSpec.builder(TypeName.get(context.sourceElement().asType()), "result",
        PRIVATE, FINAL).build();
    return new ParseResult(context, result);
  }

  public List<TypeSpec> define() {
    TypeSpec.Builder spec = classBuilder(context.parseResultType())
        .addMethod(constructorBuilder().addModifiers(PRIVATE).build())
        .addModifiers(STATIC, ABSTRACT)
        .addJavadoc("This will be a sealed type in the future.\n");
    if (context.sourceElement().getModifiers().contains(PUBLIC)) {
      spec.addModifiers(PUBLIC);
    }
    return Arrays.asList(spec.build(),
        definePrintHelpResult(),
        defineErrorResult(),
        defineSuccessResult());
  }

  private TypeSpec definePrintHelpResult() {
    TypeSpec.Builder spec = classBuilder(context.helpPrintedParseResultType())
        .superclass(context.parseResultType())
        .addModifiers(STATIC, FINAL);
    if (context.sourceElement().getModifiers().contains(PUBLIC)) {
      spec.addModifiers(PUBLIC);
    }
    return spec.build();
  }

  private TypeSpec defineErrorResult() {
    ParameterSpec paramMessage = builder(STRING, message.name).build();
    TypeSpec.Builder spec = classBuilder(context.errorParseResultType())
        .superclass(context.parseResultType())
        .addField(message)
        .addMethod(constructorBuilder()
            .addParameter(paramMessage)
            .addStatement("this.$N = $T.requireNonNull($N)", message, Objects.class, paramMessage)
            .addModifiers(PRIVATE).build())
        .addModifiers(STATIC, FINAL);
    if (context.sourceElement().getModifiers().contains(PUBLIC)) {
      spec.addModifiers(PUBLIC);
    }
    spec.addMethod(addPublicIfNecessary(context, messageMethod()));
    return spec.build();
  }

  private TypeSpec defineSuccessResult() {
    TypeSpec.Builder spec = classBuilder(context.successParseResultType())
        .superclass(context.parseResultType())
        .addField(result)
        .addMethod(successConstructor())
        .addModifiers(STATIC, FINAL);
    if (context.sourceElement().getModifiers().contains(PUBLIC)) {
      spec.addModifiers(PUBLIC);
    }
    spec.addMethod(addPublicIfNecessary(context, resultMethod()));
    return spec.build();
  }

  private MethodSpec.Builder resultMethod() {
    return methodBuilder("result")
        .addStatement("return $N", result)
        .returns(TypeName.get(context.sourceElement().asType()));
  }

  private MethodSpec.Builder messageMethod() {
    return methodBuilder("message")
        .addStatement("return $N", message)
        .returns(STRING);
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
