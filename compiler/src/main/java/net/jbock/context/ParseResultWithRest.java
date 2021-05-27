package net.jbock.context;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import net.jbock.compiler.SourceElement;

import javax.inject.Inject;
import javax.lang.model.element.Modifier;

import static com.squareup.javapoet.MethodSpec.constructorBuilder;
import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.common.Constants.STRING_ARRAY;

@ContextScope
public class ParseResultWithRest {

  private final SourceElement sourceElement;

  @Inject
  ParseResultWithRest(SourceElement sourceElement) {
    this.sourceElement = sourceElement;
  }

  TypeSpec define(ClassName resultWithRestType) {
    FieldSpec result = FieldSpec.builder(sourceElement.typeName(), "result", PRIVATE, FINAL).build();
    FieldSpec rest = FieldSpec.builder(STRING_ARRAY, "rest", PRIVATE, FINAL).build();
    return TypeSpec.classBuilder(resultWithRestType)
        .addModifiers(sourceElement.accessModifiers().toArray(new Modifier[0]))
        .addModifiers(STATIC, FINAL)
        .addField(result)
        .addField(rest)
        .addMethod(constructorBuilder()
            .addParameter(ParameterSpec.builder(result.type, result.name).build())
            .addParameter(ParameterSpec.builder(rest.type, rest.name).build())
            .addStatement("this.$N = $N", result, result)
            .addStatement("this.$N = $N", rest, rest)
            .addModifiers(PRIVATE)
            .build())
        .addMethod(methodBuilder("getRest")
            .returns(rest.type)
            .addModifiers(sourceElement.accessModifiers())
            .addStatement("return $N", rest).build())
        .addMethod(methodBuilder("getResult")
            .returns(result.type)
            .addModifiers(sourceElement.accessModifiers())
            .addStatement("return $N", result).build())
        .build();
  }
}
