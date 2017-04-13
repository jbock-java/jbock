package net.jbock.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.Arrays;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

final class ArgumentInfo {

  static TypeSpec define(ClassName argumentInfo) {
    FieldSpec lf = FieldSpec.builder(Analyser.STRING, "longName", PUBLIC, FINAL).build();
    FieldSpec sf = FieldSpec.builder(Analyser.STRING, "shortName", PUBLIC, FINAL).build();
    FieldSpec gf = FieldSpec.builder(TypeName.BOOLEAN, "flag", PUBLIC, FINAL).build();
    FieldSpec df = FieldSpec.builder(Analyser.STRING, "description", PUBLIC, FINAL).build();
    FieldSpec vf = FieldSpec.builder(Analyser.STRING, "value", PUBLIC, FINAL).build();
    ParameterSpec lp = ParameterSpec.builder(lf.type, lf.name).build();
    ParameterSpec sp = ParameterSpec.builder(sf.type, sf.name).build();
    ParameterSpec gp = ParameterSpec.builder(gf.type, gf.name).build();
    ParameterSpec dp = ParameterSpec.builder(df.type, df.name).build();
    ParameterSpec vp = ParameterSpec.builder(vf.type, vf.name).build();
    return TypeSpec.classBuilder(argumentInfo)
        .addModifiers(PUBLIC, STATIC, FINAL)
        .addFields(Arrays.asList(lf, sf, gf, df, vf))
        .addMethod(MethodSpec.constructorBuilder()
            .addModifiers(PRIVATE)
            .addParameters(Arrays.asList(lp, sp, gp, dp, vp))
            .addStatement("this.$N = $N", lf, lp)
            .addStatement("this.$N = $N", sf, sp)
            .addStatement("this.$N = $N", gf, gp)
            .addStatement("this.$N = $N", df, dp)
            .addStatement("this.$N = $N", vf, vp)
            .build())
        .build();
  }

  private ArgumentInfo() {
    throw new UnsupportedOperationException();
  }
}
