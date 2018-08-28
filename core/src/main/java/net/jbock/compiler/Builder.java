package net.jbock.compiler;

import net.jbock.com.squareup.javapoet.*;

import java.io.PrintStream;
import java.util.OptionalInt;

import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.com.squareup.javapoet.TypeName.INT;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Util.optionalOf;

final class Builder {

  final ClassName type;
  private final Context context;

  private static final int DEFAULT_INDENT = 7;

  private final FieldSpec out = FieldSpec.builder(PrintStream.class, "out")
      .initializer("$T.out", System.class)
      .addModifiers(PRIVATE).build();

  private final FieldSpec indent = FieldSpec.builder(INT, "indent")
      .initializer("$L", DEFAULT_INDENT)
      .addModifiers(PRIVATE).build();

  private final FieldSpec exitOnError = FieldSpec.builder(OptionalInt.class, "exitOnError")
      .initializer("$T.empty()", OptionalInt.class)
      .addModifiers(PRIVATE).build();

  private Builder(ClassName type, Context context) {
    this.type = type;
    this.context = context;
  }

  static Builder create(Context context) {
    ClassName builderClass = context.generatedClass.nestedClass("Builder");
    return new Builder(builderClass, context);
  }


  TypeSpec define() {
    ParameterSpec outParam = ParameterSpec.builder(out.type, out.name).build();
    ParameterSpec indentParam = ParameterSpec.builder(indent.type, indent.name).build();
    ParameterSpec exitOnErrorParam = ParameterSpec.builder(Integer.TYPE, "exitCode").build();
    TypeSpec.Builder builder = TypeSpec.classBuilder(type)
        .addModifiers(STATIC)
        .addMethod(MethodSpec.methodBuilder("out")
            .addParameter(outParam)
            .addStatement("this.$N = $N", out, outParam)
            .addStatement("return this")
            .returns(type)
            .build())
        .addMethod(MethodSpec.methodBuilder("indent")
            .addParameter(indentParam)
            .addStatement("this.$N = $N", indent, indentParam)
            .addStatement("return this")
            .returns(type)
            .build())
        .addMethod(MethodSpec.methodBuilder("onErrorExit")
            .addJavadoc("If this is set, invoking the parse method may shutdown the JVM.$W" +
                "It will shutdown in all cases where parse would return an empty value.")
            .addParameter(exitOnErrorParam)
            .addStatement("this.$N = $T.of($N)", exitOnError, OptionalInt.class, exitOnErrorParam)
            .addStatement("return this")
            .returns(type)
            .build())
        .addMethod(parseMethod())
        .addMethod(MethodSpec.constructorBuilder().addModifiers(PRIVATE)
            .build())
        .addField(indent)
        .addField(exitOnError)
        .addField(out);
    return builder.build();
  }

  private MethodSpec parseMethod() {

    ParameterSpec args = ParameterSpec.builder(ArrayTypeName.of(STRING), "args")
        .build();
    ParameterSpec result = ParameterSpec.builder(optionalOf(TypeName.get(context.sourceType.asType())), "result")
        .build();
    MethodSpec.Builder builder = MethodSpec.methodBuilder("parse");

    ParameterSpec parser = ParameterSpec.builder(context.generatedClass, "parser").build();
    builder.addStatement("$T $N = new $T($N, $N)", parser.type, parser, parser.type, out, indent);
    builder.addStatement("$T $N = $N.parse($N)", result.type, result, parser, args);
    builder.beginControlFlow("if (!$N.isPresent() && $N.isPresent())", result, exitOnError)
        .addStatement("$T.exit($N.getAsInt())", System.class, exitOnError)
        .endControlFlow();
    builder.addStatement("return $N", result);

    return builder
        .addParameter(args)
        .returns(optionalOf(TypeName.get(context.sourceType.asType())))
        .build();
  }
}
