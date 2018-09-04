package net.jbock.compiler;

import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.FieldSpec;
import net.jbock.com.squareup.javapoet.MethodSpec;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.TypeSpec;

import java.io.PrintStream;
import java.io.PrintWriter;

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
final class IndentPrinter {

  final ClassName type;

  private final FieldSpec baseIndent;
  private final FieldSpec out;
  private final FieldSpec indentLevel;

  private IndentPrinter(ClassName type, FieldSpec baseIndent, FieldSpec out, FieldSpec indentLevel) {
    this.type = type;
    this.baseIndent = baseIndent;
    this.out = out;
    this.indentLevel = indentLevel;
  }

  static IndentPrinter create(Context context) {
    FieldSpec baseIndent = FieldSpec.builder(INT, "baseIndent", FINAL).build();
    FieldSpec out = FieldSpec.builder(PrintWriter.class, "out", FINAL).build();
    FieldSpec indentLevel = FieldSpec.builder(INT, "indentLevel").build();
    ClassName type = context.generatedClass.nestedClass("IndentPrinter");
    return new IndentPrinter(type, baseIndent, out, indentLevel);
  }

  TypeSpec define() {
    return classBuilder(type)
        .addFields(asList(baseIndent, out, indentLevel))
        .addMethod(privateConstructor())
        .addMethod(methodBuilder("println")
            .addStatement("$N.println()", out)
            .build())
        .addMethod(printlnMethod())
        .addMethod(methodBuilder("incrementIndent")
            .addStatement("$N += $N", indentLevel, baseIndent)
            .build())
        .addMethod(methodBuilder("decrementIndent")
            .addStatement("$N -= $N", indentLevel, baseIndent)
            .build())
        .addMethod(methodBuilder("flush")
            .addStatement("$N.flush()", out)
            .build()).addModifiers(PRIVATE, STATIC).build();
  }

  private MethodSpec printlnMethod() {
    ParameterSpec paramText = ParameterSpec.builder(OBJECT, "text").build();
    ParameterSpec i = ParameterSpec.builder(INT, "i").build();
    return methodBuilder("println")
        .beginControlFlow("for ($T $N = $L; $N < $N; $N++)", INT, i, 0, i, indentLevel, i)
        .addStatement("$N.print(' ')", out)
        .endControlFlow()
        .addStatement("$N.println($N)", out, paramText)
        .addParameter(paramText)
        .build();
  }

  private MethodSpec privateConstructor() {
    ParameterSpec paramOut = ParameterSpec.builder(PrintStream.class, "out").build();
    ParameterSpec paramBaseIndent = ParameterSpec.builder(INT, "baseIndent").build();
    return MethodSpec.constructorBuilder()
        .addParameter(paramOut)
        .addParameter(paramBaseIndent)
        .addStatement("this.$N = new $T($N)", out, PrintWriter.class, paramOut)
        .addStatement("this.$N = $N", baseIndent, paramBaseIndent)
        .build();
  }
}
