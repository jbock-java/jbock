package net.jbock.compiler.view;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import net.jbock.compiler.Context;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Objects;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.TypeName.INT;
import static com.squareup.javapoet.TypeSpec.classBuilder;
import static java.util.Arrays.asList;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Constants.STRING;

/**
 * Defines the inner class IndentPrinter.
 */
final class IndentPrinter {

  private final Context context;
  private final FieldSpec baseIndent;
  private final FieldSpec out;
  private final FieldSpec indentLevel;

  private IndentPrinter(Context context, FieldSpec baseIndent, FieldSpec out, FieldSpec indentLevel) {
    this.context = context;
    this.baseIndent = baseIndent;
    this.out = out;
    this.indentLevel = indentLevel;
  }

  static IndentPrinter create(Context context) {
    FieldSpec baseIndent = FieldSpec.builder(INT, "baseIndent", FINAL).build();
    FieldSpec out = FieldSpec.builder(PrintWriter.class, "out", FINAL).build();
    FieldSpec indentLevel = FieldSpec.builder(INT, "indentLevel").build();
    return new IndentPrinter(context, baseIndent, out, indentLevel);
  }

  TypeSpec define() {
    return classBuilder(context.indentPrinterType())
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
    ParameterSpec paramText = ParameterSpec.builder(STRING, "text").build();
    ParameterSpec i = ParameterSpec.builder(INT, "i").build();
    MethodSpec.Builder spec = methodBuilder("println");
    spec.beginControlFlow("if ($T.toString($N, $S).isEmpty())", Objects.class, paramText, "")
        .addStatement("$N.println()", out)
        .addStatement("return")
        .endControlFlow();
    spec.beginControlFlow("for ($T $N = $L; $N < $N; $N++)", INT, i, 0, i, indentLevel, i)
        .addStatement("$N.print(' ')", out)
        .endControlFlow();
    spec.addStatement("$N.println($N)", out, paramText);
    return spec.addParameter(paramText)
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
